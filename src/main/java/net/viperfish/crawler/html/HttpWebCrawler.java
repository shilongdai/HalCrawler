package net.viperfish.crawler.html;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.viperfish.crawler.core.DataProcessor;
import net.viperfish.crawler.core.Datasink;
import net.viperfish.crawler.core.ProcessedResult;
import net.viperfish.crawler.exceptions.ParsingException;
import net.viperfish.framework.compression.Compressor;
import net.viperfish.framework.compression.Compressors;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

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
public class HttpWebCrawler extends DataProcessor<FetchedContent, CrawledData> {

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
	private List<HttpCrawlerHandler> httpCrawlerHandler;
	private HttpFetcher fetcher;

	/**
	 * creates a new crawler with supplied storage output for site data and anchor data, and the
	 * {@link HttpFetcher} to be used for downloading sites.
	 *
	 * @param threadCount the amount of processing threads
	 * @param db the storage output for the crawled sites
	 * @param fetcher the {@link HttpFetcher} for fetching contents
	 */
	public HttpWebCrawler(int threadCount, Datasink<? super CrawledData> db,
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
		httpCrawlerHandler = new LinkedList<>();
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

	public List<HttpCrawlerHandler> getHttpCrawlerHandlers() {
		return httpCrawlerHandler;
	}

	public void registerCrawlerHandler(HttpCrawlerHandler checker) {
		this.httpCrawlerHandler.add(checker);
	}

	@Override
	protected ProcessedResult<CrawledData> process(FetchedContent content) throws ParsingException {
		if (!ACCEPTED_STATUS_CODE.contains(content.getStatus())) {
			return null;
		}
		String cleanHTML = Jsoup.clean(content.getHtml(), content.getUrl().toExternalForm(),
			Whitelist.relaxed().addTags("title").addTags("head"));
		content.setHtml(cleanHTML);
		CrawledData site = toSite(content);
		boolean shouldIndex = true;
		// parse document
		Document doc = Jsoup.parse(cleanHTML);
		site.setAnchors(extractAnchors(doc, site.getUrl()));

		// do post parse operations
		HandlerResponse postParseResponse = HandlerResponse.GO_AHEAD;

		for (HttpCrawlerHandler handler : httpCrawlerHandler) {
			HandlerResponse resp = handler.handlePostParse(site);
			if (resp.overrides(postParseResponse)) {
				postParseResponse = resp;
			}
		}

		if (postParseResponse == HandlerResponse.HALT) {
			return null;
		}
		if (postParseResponse == HandlerResponse.DEFERRED) {
			fetcher.submit(site.getUrl());
			return null;
		}
		if (postParseResponse == HandlerResponse.NO_INDEX) {
			shouldIndex = false;
		}

		// get and process all of the tags
		recursiveInterpretTags(
			doc.getElementsByTag("html").first(), site);
		site.setTitle(extractTitle(content.getUrl(), doc));

		// do post process operations
		HandlerResponse postProcessResponse = HandlerResponse.GO_AHEAD;
		for (HttpCrawlerHandler handler : httpCrawlerHandler) {
			HandlerResponse resp = handler.handlePostProcess(site);
			if (resp.overrides(postProcessResponse)) {
				postProcessResponse = resp;
			}
		}

		if (postProcessResponse == HandlerResponse.HALT) {
			return null;
		}
		if (postProcessResponse == HandlerResponse.DEFERRED) {
			fetcher.submit(site.getUrl());
			return null;
		}
		if (postProcessResponse == HandlerResponse.NO_INDEX) {
			shouldIndex = false;
		}

		// add pages to crawl
		for (Anchor anchor : site.getAnchors()) {
			HandlerResponse anchorResponse = HandlerResponse.GO_AHEAD;
			for (HttpCrawlerHandler handler : httpCrawlerHandler) {
				HandlerResponse resp = handler.handlePreFetch(anchor.getTargetURL());
				if (resp.overrides(anchorResponse)) {
					anchorResponse = resp;
				}
			}
			// make sure not repeating
			if (anchorResponse == HandlerResponse.GO_AHEAD) {
				fetcher.submit(anchor.getTargetURL());
			}
		}

		return new ProcessedResult<CrawledData>(site, shouldIndex);
	}

	private CrawledData toSite(FetchedContent content) {
		String checksum = hashSite(content.getHtml().getBytes(StandardCharsets.UTF_8));
		CrawledData result = new CrawledData();
		result.setChecksum(checksum);
		result.setContent(content.getHtml());
		result.setUrl(content.getUrl());
		return result;
	}

	private void recursiveInterpretTags(Element e, CrawledData s)
		throws ParsingException {
		if (e == null || e.tagName() == null) {
			return;
		}

		for (TagProcessor processor : processors.values()) {
			if (processor.match(e)) {
				processor.processTag(e, s);
			}
		}

		for (Element child : e.children()) {
			recursiveInterpretTags(child, s);
		}
	}

	private String extractTitle(URL url, Document document) {
		Elements elements = document.select("title");
		if (!elements.isEmpty()) {
			return elements.get(0).text();
		}
		return url.toString();
	}

	private String hashSite(byte[] compressed) {
		Digest dig = hasher.get();
		dig.reset();
		dig.update(compressed, 0, compressed.length);
		byte[] out = new byte[16];
		dig.doFinal(out, 0);
		return Base64.getEncoder().encodeToString(out);
	}

	private List<Anchor> extractAnchors(Document document, URL siteURL) {
		List<Anchor> anchors = new LinkedList<>();
		Elements links = document.select("a");
		for (Element e : links) {
			String href = e.attr("abs:href");
			if (href == null || href.trim().isEmpty()) {
				continue;
			}
			try {
				if (isRelative(href)) {
					if (href.startsWith("/")) {
						href = parseRelativeDirectly2Host(siteURL, href);
					} else {
						URL nearestDir = getNearestPath(siteURL);
						href = new URL(nearestDir, href).toString();
					}
				}
				if (href.length() < 8) {
					continue;
				}
				Anchor anchor = new Anchor();
				URL anchorURL = new URL(href);
				anchor.setAnchorText(e.text());
				anchor.setTargetURL(anchorURL);
				anchor.setSize(16);
				anchors.add(anchor);
			} catch (MalformedURLException error) {
				System.out.println("Invalid URL:" + error.getMessage());
			}
		}
		return anchors;
	}

	private boolean isRelative(String url) {
		return !(url.startsWith("http://") || url.startsWith("https://"));
	}

	private String parseRelativeDirectly2Host(URL base, String url) {
		StringBuilder sb = new StringBuilder(base.getProtocol());
		sb.append("://").append(base.getHost()).append(url);
		return sb.toString();
	}

	private URL getNearestPath(URL base) throws MalformedURLException {
		return new URL(
			base.toExternalForm().substring(0, base.toExternalForm().lastIndexOf("/") + 1));
	}

}
