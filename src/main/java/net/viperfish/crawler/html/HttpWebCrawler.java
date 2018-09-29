package net.viperfish.crawler.html;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.viperfish.crawler.core.DataProcessor;
import net.viperfish.crawler.core.Datasink;
import net.viperfish.crawler.exceptions.ParsingException;
import net.viperfish.crawler.html.crawlChecker.YesCrawlChecker;
import net.viperfish.framework.compression.Compressor;
import net.viperfish.framework.compression.Compressors;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

// TODO: refractor the whole thing into more modular structure. Add documentation

/**
 * A {@link DataProcessor} that crawls http pages. It takes raw html contents and extracts
 * information from it. By default, this implementation will fill in the URL and Compressed HTML
 * attributes of a site. To make this crawler follow the links on a page, scan text content etc, a
 * custom {@link TagProcessor} need to be supplied through the {@link
 * HttpWebCrawler#registerProcessor(String, TagProcessor)} method. The output written by this class
 * will contain all the successfully retrieved crawled sites. Pages with response code not included
 * in the 2xx codes are discarded. This implementation is designed for concurrency.
 */
public class HttpWebCrawler extends DataProcessor<FetchedContent, Site> {

	// the crawler will save codes contained in this set
	private static final Set<Integer> ACCEPTED_STATUS_CODE;

	static {
		Set<Integer> buffer = new HashSet<>();
		buffer.add(200);
		buffer.add(201);
		buffer.add(202);
		buffer.add(203);
		buffer.add(302);
		ACCEPTED_STATUS_CODE = Collections.unmodifiableSet(buffer);
	}

	private Map<String, TagProcessor> processors;
	private boolean limit2Host;
	private ThreadLocal<Digest> hasher;
	private Compressor compressor;
	private HttpCrawlerHandler httpCrawlerHandler;
	private HttpFetcher fetcher;

	/**
	 * creates a new crawler with supplied storage output for site data and anchor data, and the
	 * {@link HttpFetcher} to be used for downloading sites.
	 *
	 * @param threadCount the amount of processing threads
	 * @param db the storage output for the crawled sites
	 * @param fetcher the {@link HttpFetcher} for fetching contents
	 */
	public HttpWebCrawler(int threadCount, Datasink<? super Site> db,
		HttpFetcher fetcher) {
		super(fetcher, db, threadCount);
		processors = new HashMap<>();
		this.fetcher = fetcher;
		limit2Host = false;
		hasher = new ThreadLocal<Digest>() {

			@Override
			protected Digest initialValue() {
				return new MD5Digest();
			}

		};
		try {
			compressor = Compressors.getCompressor("GZ");
		} catch (NoSuchAlgorithmException e) {
			// Guaranteed not gonna happen
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		httpCrawlerHandler = new YesCrawlChecker();
	}

	public void submit(URL url) {
		fetcher.submit(url);
	}

	public void registerProcessor(String processorName, TagProcessor processor) {
		processors.put(processorName, processor);
	}

	public void unregisterProcessor(String processorName) {
		processors.remove(processorName);
	}

	public TagProcessor getProcessor(String tagName) {
		return processors.get(tagName);
	}

	public void limitToHost(boolean limit) {
		this.limit2Host = limit;
	}

	public boolean isLimitedToHost() {
		return limit2Host;
	}

	public HttpCrawlerHandler getHttpCrawlerHandler() {
		return httpCrawlerHandler;
	}

	public void setHttpCrawlerHandler(HttpCrawlerHandler checker) {
		this.httpCrawlerHandler = checker;
	}

	@Override
	protected Site process(FetchedContent content) {
		try {
			if (!ACCEPTED_STATUS_CODE.contains(content.getStatus())) {
				return null;
			}
			String cleanHTML = Jsoup.clean(content.getHtml(), content.getUrl().toExternalForm(),
				Whitelist.relaxed().addTags("title").addTags("head"));
			content.setHtml(cleanHTML);
			Site site = toSite(content);

			// do post parse operations
			HandlerResponse postParseResp = httpCrawlerHandler.handlePostParse(site);
			if (postParseResp == HandlerResponse.HALT) {
				return null;
			}
			if (postParseResp == HandlerResponse.DEFERRED) {
				fetcher.submit(site.getUrl());
				return null;
			}

			Document doc = Jsoup.parse(cleanHTML);

			// get and process all of the tags
			Map<TagDataType, List<TagData>> processedTags = recursiveInterpretTags(
				doc.getElementsByTag("html").first(), site);

			// process stuff on the page
			processTitle(processedTags, site);
			processHeaders(processedTags, site);
			processTexts(processedTags, site);
			processEmphasizedText(processedTags, site);

			// do post process operations
			HandlerResponse postProcessResp = httpCrawlerHandler.handlePostProcess(site);
			if (postProcessResp == HandlerResponse.HALT) {
				return null;
			}
			if (postProcessResp == HandlerResponse.DEFERRED) {
				fetcher.submit(site.getUrl());
				return null;
			}

			// add pages to crawl
			if (processedTags.containsKey(TagDataType.HTML_LINK)) {
				List<TagData> linkTags = processedTags.get(TagDataType.HTML_LINK);
				List<Anchor> anchors = new LinkedList<>();
				for (TagData td : linkTags) {
					Anchor anchor = toAnchor(td);
					anchors.add(anchor);
					// check if limited to host
					if (limit2Host) {
						if (!anchor.getTargetURL().getHost().equals(content.getUrl().getHost())) {
							continue;
						}
					}

					// make sure not repeating
					if (httpCrawlerHandler.handlePreFetch(anchor.getTargetURL())
						== HandlerResponse.GO_AHEAD) {
						fetcher.submit(anchor.getTargetURL());
					}
				}
				site.setAnchors(anchors);
			}
			return site;
		} catch (ParsingException e) {
			System.out.println("Parsing Error:" + e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println("Page Not Found:" + e.getMessage());
		} catch (IOException e) {
			if (e.getCause() instanceof SQLException) {
				System.out.println("Database Error:" + e.getMessage());
			} else {
				System.out.println("Http Error:" + e.getMessage());
			}
			e.printStackTrace();
		}
		return null;
	}

	private Site toSite(FetchedContent content) {
		byte[] compressed = this.compressor
			.compress(content.getHtml().getBytes(StandardCharsets.UTF_16));
		String checksum = hashSite(compressed);

		Site result = new Site();
		result.setChecksum(checksum);
		result.setCompressedHtml(compressed);
		result.setUrl(content.getUrl());
		return result;
	}

	private Map<TagDataType, List<TagData>> recursiveInterpretTags(Element e, Site s)
		throws ParsingException {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		if (e == null || e.tagName() == null) {
			return result;
		}

		for (TagProcessor processor : processors.values()) {
			if (processor.match(e)) {
				result.putAll(processor.processTag(e, s));
			}
		}

		for (Element child : e.children()) {
			Map<TagDataType, List<TagData>> childResult = recursiveInterpretTags(child, s);
			for (Entry<TagDataType, List<TagData>> entry : childResult.entrySet()) {
				if (!result.containsKey(entry.getKey())) {
					result.put(entry.getKey(), entry.getValue());
				} else {
					for (TagData td : entry.getValue()) {
						result.get(entry.getKey()).add(td);
					}
				}
			}
		}
		return result;
	}

	private String hashSite(byte[] compressed) {
		Digest dig = hasher.get();
		dig.reset();
		dig.update(compressed, 0, compressed.length);
		byte[] out = new byte[16];
		dig.doFinal(out, 0);
		return Base64.getEncoder().encodeToString(out);
	}

	private void processHeaders(Map<TagDataType, List<TagData>> processedTags, Site site) {
		// parse headers
		if (processedTags.containsKey(TagDataType.HTML_HEADER_CONTENT)) {
			List<TagData> headers = processedTags.get(TagDataType.HTML_HEADER_CONTENT);
			for (TagData td : headers) {
				Header header = new Header();
				header.setContent(td.get("headerText", String.class));
				header.setSize(Integer.parseInt(td.get("size", String.class)));
				site.getHeaders().add(header);
			}
		}
	}

	private void processTexts(Map<TagDataType, List<TagData>> processedTags, Site site) {
		// parse headers
		if (processedTags.containsKey(TagDataType.HTML_TEXT_CONTENT)) {
			List<TagData> headers = processedTags.get(TagDataType.HTML_TEXT_CONTENT);
			for (TagData td : headers) {
				TextContent text = new TextContent();
				text.setContent(td.get("text", String.class));
				site.getTexts().add(text);
			}
		}
	}

	private void processEmphasizedText(Map<TagDataType, List<TagData>> processedTags, Site site) {
		// parse headers
		if (processedTags.containsKey(TagDataType.HTML_EMPHASIZED_TEXT)) {
			List<TagData> headers = processedTags.get(TagDataType.HTML_EMPHASIZED_TEXT);
			for (TagData td : headers) {
				EmphasizedTextContent text = new EmphasizedTextContent();
				text.setContent(td.get("content", String.class));
				text.setMethod(td.get("method", EmphasizedType.class));
				site.getTexts().add(text);
			}
		}
	}

	private void processTitle(Map<TagDataType, List<TagData>> processedTags, Site site) {
		// parse title
		if (processedTags.containsKey(TagDataType.HTML_TITLE)) {
			site.setTitle(
				processedTags.get(TagDataType.HTML_TITLE).get(0).get("title", String.class));
		}
	}

	private Anchor toAnchor(TagData anchorData) throws IOException {
		URL linkedPage = anchorData.get("url", URL.class);
		String anchorName = anchorData.get("anchor", String.class);

		Anchor result = new Anchor();
		result.setAnchorText(anchorName);
		result.setTargetURL(linkedPage);
		return result;
	}


}
