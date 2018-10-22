package net.viperfish.crawler.html;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import net.viperfish.crawler.html.exception.ParsingException;
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
 * information from it. By default, without any additional processors, it will fill in all the
 * built-in attributes in the {@link CrawledData} class. The output written by this class will
 * contain all the successfully retrieved crawled sites. Pages with response code not included in
 * the 2xx codes are discarded. To control the flow of processing, or insert code during the
 * processing, implement and register a {@link HttpCrawlerHandler}. To customize/specialize the
 * output of this class, implement and register a {@link TagProcessor}.
 */
public class HttpWebCrawler extends DataProcessor<FetchedContent, CrawledData> {

	// the crawler will save codes contained in this set
	private static final Set<Integer> ACCEPTED_STATUS_CODE;
	private static final String DOC_ATTR;


	static {
		Set<Integer> buffer = new HashSet<>();
		buffer.add(200);
		buffer.add(201);
		buffer.add(202);
		buffer.add(203);
		buffer.add(302);
		ACCEPTED_STATUS_CODE = Collections.unmodifiableSet(buffer);
		DOC_ATTR = "_doc";
	}

	private Map<String, TagProcessor> processors;
	private ThreadLocal<Digest> hasher;
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
	public HttpWebCrawler(int threadCount, Datasink<CrawledData> db,
		HttpFetcher fetcher) {
		super(fetcher, db, threadCount);
		processors = new HashMap<>();
		this.fetcher = fetcher;
		hasher = new ThreadLocal<Digest>() {

			@Override
			protected Digest initialValue() {
				return new MD5Digest();
			}

		};
		httpCrawlerHandler = new LinkedList<>();
	}

	/**
	 * submits URL to be fetched and processed. This will skip over all the {@link
	 * HttpCrawlerHandler#handlePreFetch(URL)}.
	 *
	 * @param url the URl to be fetched and processed.
	 */
	public void submit(URL url) {
		fetcher.submit(url, Integer.MAX_VALUE);
	}

	/**
	 * registers a {@link TagProcessor} to customize the crawled output.
	 *
	 * @param processorName the unique name of the processor.
	 * @param processor the processor itself.
	 */
	public void registerProcessor(String processorName, TagProcessor processor) {
		processors.put(processorName, processor);
	}

	/**
	 * removes a processor by its name.
	 *
	 * @param processorName the name of the processor.
	 */
	public void unregisterProcessor(String processorName) {
		processors.remove(processorName);
	}

	/**
	 * gets a registered processor by its name.
	 *
	 * @param name the name of the processor.
	 * @return the registered processor with the name or null.
	 */
	public TagProcessor getProcessor(String name) {
		return processors.get(name);
	}

	/**
	 * gets all the registered handler attached to the crawler.
	 *
	 * @return the list of {@link HttpCrawlerHandler}.
	 */
	public List<HttpCrawlerHandler> getHttpCrawlerHandlers() {
		return httpCrawlerHandler;
	}

	/**
	 * registers a {@link HttpCrawlerHandler} to control and perform operation during the
	 * processing.
	 *
	 * @param checker the handler to register.
	 */
	public void registerCrawlerHandler(HttpCrawlerHandler checker) {
		this.httpCrawlerHandler.add(checker);
	}

	@Override
	protected ProcessedResult<CrawledData> process(FetchedContent content) throws ParsingException {
		if (!ACCEPTED_STATUS_CODE.contains(content.getStatus())) {
			return null;
		}
		boolean shouldIndex = true;

		HandlerResponse preParseResp = HandlerResponse.GO_AHEAD;
		for (HttpCrawlerHandler handler : httpCrawlerHandler) {
			HandlerResponse resp = handler.handlePreParse(content);
			if (resp.overrides(preParseResp)) {
				preParseResp = resp;
			}
		}
		if (preParseResp == HandlerResponse.HALT) {
			return null;
		}
		if (preParseResp == HandlerResponse.DEFERRED) {
			fetcher.submit(content.getUrl().getToFetch());
			return null;
		}
		if (preParseResp == HandlerResponse.NO_INDEX) {
			shouldIndex = false;
		}

		CrawledData site = parseFetchedContent(content);
		Document doc = site.getProperty(DOC_ATTR, Document.class);
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

		// process the document
		processDocument(doc, site);
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

		submitAnchors(site);
		site.getProperties().remove(DOC_ATTR);
		return new ProcessedResult<>(site, shouldIndex);
	}

	/**
	 * submits all the parsed anchors from a site to be crawled in future.
	 *
	 * @param site the root site.
	 */
	private void submitAnchors(CrawledData site) {
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
	}

	/**
	 * parses and cleans the fetched site into a {@link CrawledData}.
	 *
	 * @param content the content fetched.
	 * @return the parsed {@link CrawledData}.
	 */
	private CrawledData parseFetchedContent(FetchedContent content) {
		String cleanHTML = Jsoup
			.clean(content.getHtml(), content.getUrl().getToFetch().toExternalForm(),
				Whitelist.relaxed().addTags("title").addTags("head"));
		CrawledData site = toSite(content);
		// parse document
		Document doc = Jsoup.parse(cleanHTML);
		site.setAnchors(extractAnchors(doc, site.getUrl()));
		site.setTitle(extractTitle(content.getUrl().getToFetch(), doc));
		site.setProperty(DOC_ATTR, doc);
		return site;
	}

	/**
	 * converts the fetched content into a {@link CrawledData}.
	 *
	 * @param content the fetched content
	 * @return a crawled data built from the fetched content.
	 */
	private CrawledData toSite(FetchedContent content) {
		String checksum = hashSite(content.getHtml().getBytes(StandardCharsets.UTF_8));
		CrawledData result = new CrawledData();
		result.setChecksum(checksum);
		result.setContent(content.getHtml());
		result.setUrl(content.getUrl().getToFetch());
		return result;
	}

	/**
	 * processes the document with the tag processors.
	 *
	 * @param doc the document to process
	 * @param s the crawled data
	 * @throws ParsingException if failed to parse
	 */
	private void processDocument(Document doc, CrawledData s)
		throws ParsingException {
		for (TagProcessor t : this.processors.values()) {
			t.processTag(doc, s);
		}
	}

	/**
	 * extracts the title from the html document. If no title found, then the string representation
	 * of the URL is returned.
	 *
	 * @param url the URL of the site.
	 * @param document the parsed HTML document.
	 * @return the title of the site.
	 */
	private String extractTitle(URL url, Document document) {
		Elements elements = document.select("title");
		if (!elements.isEmpty()) {
			return elements.get(0).text();
		}
		return url.toString();
	}

	/**
	 * generates the checksum of the site.
	 *
	 * @param html the html bytes.
	 * @return a base64 encoded checksum.
	 */
	private String hashSite(byte[] html) {
		Digest dig = hasher.get();
		dig.reset();
		dig.update(html, 0, html.length);
		byte[] out = new byte[16];
		dig.doFinal(out, 0);
		return Base64.getEncoder().encodeToString(out);
	}

	// TODO: add support for size parsing for anchors.

	/**
	 * gets all the anchors on a html page. It converts all the relative URLs to absolute URLs.
	 *
	 * @param document the html document.
	 * @param siteURL the URL of the site.
	 * @return a list of anchors on the page.
	 */
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

	/**
	 * checks if a URL is a relative URL.
	 *
	 * @param url the url string.
	 * @return true if the url is relative, false otherwise.
	 */
	private boolean isRelative(String url) {
		return !(url.startsWith("http://") || url.startsWith("https://"));
	}

	/**
	 * change a relative url in the path format to the absolute URL.
	 *
	 * @param base the base URL of the host.
	 * @param url the relative URL.
	 * @return the absolute URL.
	 */
	private String parseRelativeDirectly2Host(URL base, String url) {
		StringBuilder sb = new StringBuilder(base.getProtocol());
		sb.append("://").append(base.getHost()).append(url);
		return sb.toString();
	}

	/**
	 * gets the URL to the current directory on the host.
	 *
	 * @return the URL of the current directory.
	 * @throws MalformedURLException if the result is not a good URL.
	 */
	private URL getNearestPath(URL base) throws MalformedURLException {
		return new URL(
			base.toExternalForm().substring(0, base.toExternalForm().lastIndexOf("/") + 1));
	}

}
