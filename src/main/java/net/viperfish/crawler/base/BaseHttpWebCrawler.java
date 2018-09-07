package net.viperfish.crawler.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.core.Anchor;
import net.viperfish.crawler.core.DatabaseObject;
import net.viperfish.crawler.core.EmphasizedTextContent;
import net.viperfish.crawler.core.EmphasizedType;
import net.viperfish.crawler.core.Header;
import net.viperfish.crawler.core.HttpWebCrawler;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.SiteDatabase;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.core.TextContent;
import net.viperfish.crawler.crawlChecker.NullCrawlChecker;
import net.viperfish.crawler.exceptions.ParsingException;
import net.viperfish.framework.compression.Compressor;
import net.viperfish.framework.compression.Compressors;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

// TODO: refractor the whole thing into more modular structure. Add documentation

public class BaseHttpWebCrawler implements HttpWebCrawler {

	private static final Set<Integer> ACCEPTED_STATUS_CODE;
	private static int THREAD_COUNT = 32;
	private static URL NULL;

	static {
		Set<Integer> buffer = new HashSet<>();
		buffer.add(200);
		buffer.add(201);
		buffer.add(202);
		buffer.add(203);
		ACCEPTED_STATUS_CODE = Collections.unmodifiableSet(buffer);
		try {
			NULL = new URL("https://none.org");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private Map<String, TagProcessor> processors;
	private boolean limit2Host;
	private ExecutorService executor;
	private ThreadLocal<Digest> hasher;
	private SiteDatabase db;
	private DatabaseObject<Long, Anchor> anchorDB;
	private Compressor compressor;
	private CrawlChecker crawlChecker;
	private BlockingQueue<URL> toCrawl;
	private BlockingQueue<Long> results;
	private AtomicInteger activeThreads;

	public BaseHttpWebCrawler(SiteDatabase db, DatabaseObject<Long, Anchor> anchorDB) {
		processors = new HashMap<>();
		this.db = db;
		this.anchorDB = anchorDB;
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
		executor = Executors.newFixedThreadPool(THREAD_COUNT);
		crawlChecker = new NullCrawlChecker();
		toCrawl = new LinkedBlockingQueue<>();
		results = new LinkedBlockingQueue<>();
		activeThreads = new AtomicInteger(0);

		startCrawlers();
	}

	@Override
	public void submit(URL url) {
		toCrawl.offer(url);
	}

	public void registerProcessor(String tagName, TagProcessor processor) {
		processors.put(tagName, processor);
	}

	public void unregisterProcessor(String tagName) {
		processors.remove(tagName);
	}

	public TagProcessor getProcessor(String tagName) {
		return processors.get(tagName);
	}

	@Override
	public void limitToHost(boolean limit) {
		this.limit2Host = limit;
	}

	@Override
	public boolean isLimitedToHost() {
		return limit2Host;
	}

	@Override
	public void shutdown() {
		for (int i = 0; i < THREAD_COUNT; ++i) {
			toCrawl.offer(NULL);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			executor.shutdownNow();
			return;
		}
	}

	public CrawlChecker getCrawlChecker() {
		return crawlChecker;
	}

	public void setCrawlChecker(CrawlChecker checker) {
		this.crawlChecker = checker;
	}

	public BlockingQueue<Long> getResults() {
		return results;
	}

	@Override
	public boolean isIdle() {
		return activeThreads.get() == 0;
	}

	private void crawlIterative(URL url) {
		try {
			if (!crawlChecker.shouldCrawl(url)) {
				return;
			}
			Site site = new Site();
			if (!fetchSite(url, site)) {
				return;
			}
			if (!crawlChecker.shouldCrawl(url, site)) {
				return;
			}
			if (!crawlChecker.lock(url, site)) {
				return;
			}

			// get and process all of the tags
			Map<TagDataType, List<TagData>> processedTags = recursiveInterpretTags(
				site.getDoc().getElementsByTag("html").first(), site);

			// process stuff on the page
			processTitle(processedTags, site);
			processHeaders(processedTags, site);
			processTexts(processedTags, site);
			processEmphasizedText(processedTags, site);

			// save site and gc
			db.save(site);
			long siteID = site.getSiteID();
			results.offer(siteID);
			site = null;

			// add pages to crawl
			if (processedTags.containsKey(TagDataType.HTML_LINK)) {
				List<TagData> linkTags = processedTags.get(TagDataType.HTML_LINK);
				List<Anchor> anchors = new LinkedList<>();
				for (TagData td : linkTags) {
					Anchor anchor = toAnchor(td, siteID);
					anchors.add(anchor);
					// check if limited to host
					if (limit2Host) {
						if (!anchor.getTargetURL().getHost().equals(url.getHost())) {
							continue;
						}
					}
					toCrawl.offer(anchor.getTargetURL());
				}
				anchorDB.save(anchors);
			}
		} catch (ParsingException e) {
			System.out.println("Error Parsing:" + url);
		} catch (FileNotFoundException e) {
			System.out.println("HTTP FILE NOT FOUND:" + url);
		} catch (IOException e) {
			if (e.getCause() instanceof SQLException) {
				System.out.println("Database Error:" + e.getMessage());
			} else {
				System.out.println("Http Error:" + e.getMessage());
			}
			e.printStackTrace();
		}
	}

	private Map<TagDataType, List<TagData>> recursiveInterpretTags(Element e, Site s)
		throws ParsingException {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		if (e == null || e.tagName() == null) {
			return result;
		}
		TagProcessor processor = processors.get(e.tagName());
		if (processor != null) {
			result = processor.processTag(e, s);
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

	private boolean isHtml(URL url) throws IOException {
		HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
		try {
			urlc.setRequestMethod("HEAD");
			urlc.connect();
			String mime = urlc.getContentType();
			if (mime.contains("text/html") || mime.contains("text/htm") || mime
				.contains("text/plain")) {
				return true;
			}
			return false;
		} catch (Throwable e) {
			return false;
		} finally {
			urlc.disconnect();
		}
	}

	private boolean fetchSite(URL url, Site site) throws IOException {
		URLConnection conn = url.openConnection();
		HttpURLConnection urlc = null;
		if (url.openConnection() instanceof HttpURLConnection) {
			// establish connection
			urlc = (HttpURLConnection) conn;
		} else {
			return false;
		}
		try {
			// make sure html
			if (!isHtml(url)) {
				System.out.println("not html");
				return false;
			}

			urlc.setAllowUserInteraction(false);
			urlc.setDoInput(true);
			urlc.setDoOutput(false);
			urlc.setUseCaches(true);

			// fetch content
			urlc.setRequestMethod("GET");
			urlc.connect();
			if (ACCEPTED_STATUS_CODE.contains(urlc.getResponseCode())) {
				String contentEncoding = urlc.getContentEncoding();
				if (contentEncoding == null) {
					contentEncoding = "UTF-8";
				}
				String pageHtml = new String(IOUtil.read(urlc.getInputStream()), contentEncoding);

				// process page
				String pageChecksum = hashSite(pageHtml);
				String cleanedHTML = Jsoup.clean(pageHtml, url.toExternalForm(),
					Whitelist.relaxed().addTags("title").addTags("head"));
				Document doc = Jsoup.parse(cleanedHTML, url.toExternalForm());
				// compresses the html
				byte[] compressedHtm = compressor
					.compress(pageHtml.getBytes(StandardCharsets.UTF_16));

				// save result to site
				site.setUrl(url);
				site.setChecksum(pageChecksum);
				site.setCompressedHtml(compressedHtm);
				site.setDoc(doc);
				return true;
			} else {
				System.out.println("Not Successful:" + urlc.getResponseCode());
				return false;
			}
		} finally {
			if (urlc != null) {
				urlc.disconnect();
			}
		}
	}

	private String hashSite(String html) {
		Digest dig = hasher.get();
		dig.reset();
		byte[] inputBytes = html.getBytes(StandardCharsets.UTF_16);
		dig.update(inputBytes, 0, inputBytes.length);
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
		if (processedTags.containsKey(TagDataType.HTML_TEXT_CONTENT)) {
			List<TagData> headers = processedTags.get(TagDataType.HTML_TEXT_CONTENT);
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

	private Anchor toAnchor(TagData anchorData, long siteID) throws IOException {
		URL linkedPage = anchorData.get("url", URL.class);
		String anchorName = anchorData.get("anchor", String.class);

		Anchor result = new Anchor();
		result.setAnchorText(anchorName);
		result.setTargetURL(linkedPage);
		result.setSiteID(siteID);
		return result;
	}

	private void startCrawlers() {
		for (int i = 0; i < THREAD_COUNT; ++i) {
			executor.execute(new Runnable() {

				@Override
				public void run() {
					while (!Thread.interrupted()) {
						URL next;
						try {
							next = toCrawl.take();
						} catch (InterruptedException e1) {
							return;
						}

						if (next == NULL) {
							return;
						}

						try {
							activeThreads.incrementAndGet();
							crawlIterative(next);
						} catch (Throwable e) {
							e.printStackTrace();
						} finally {
							activeThreads.decrementAndGet();
						}
					}
				}
			});
		}
	}

}
