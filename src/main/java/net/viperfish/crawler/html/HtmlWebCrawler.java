package net.viperfish.crawler.html;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import net.viperfish.crawler.core.Anchor;
import net.viperfish.crawler.core.DatabaseObject;
import net.viperfish.crawler.core.Header;
import net.viperfish.crawler.core.HttpWebCrawler;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.SiteDatabase;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.core.TextContent;
import net.viperfish.crawler.exceptions.ParsingException;
import net.viperfish.framework.compression.Compressor;
import net.viperfish.framework.compression.Compressors;

public class HtmlWebCrawler implements HttpWebCrawler {

	private static int THREAD_COUNT = 64;
	private static final Set<Integer> ACCEPTED_STATUS_CODE;

	static {
		Set<Integer> buffer = new HashSet<>();
		buffer.add(200);
		buffer.add(201);
		buffer.add(202);
		buffer.add(203);
		ACCEPTED_STATUS_CODE = Collections.unmodifiableSet(buffer);
	}

	private Map<String, TagProcessor> processors;
	private boolean limit2Host;
	private ExecutorService executor;
	private ThreadLocal<Digest> hasher;
	private SiteDatabase db;
	private DatabaseObject<Long, Anchor> anchorDB;
	private Compressor compressor;
	private AtomicInteger busyThreads;

	public HtmlWebCrawler(SiteDatabase db, DatabaseObject<Long, Anchor> anchorDB) {
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
		busyThreads = new AtomicInteger(0);
	}

	@Override
	public Map<Long, URL> crawl(URL url) {
		ConcurrentMap<URL, String> crawled = new ConcurrentHashMap<>();
		ConcurrentMap<Long, URL> result = new ConcurrentHashMap<>();
		crawlRecursive(url, crawled, result);
		return result;
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

	private void crawlRecursive(URL url, ConcurrentMap<URL, String> crawledSites, ConcurrentMap<Long, URL> result) {
		System.out.println("Crawling:" + url);
		try {
			if (didCrawl(crawledSites, url)) {
				return;
			}
			if (!crawled(crawledSites, url, "")) {
				return;
			}

			Site site = new Site();
			if (!fetchSite(url, site)) {
				return;
			}

			System.out.println("Processing:" + url);

			// get and process all of the tags
			Map<TagDataType, List<TagData>> processedTags = recursiveInterpretTags(
					site.getDoc().getElementsByTag("html").first(), site);

			// process stuff on the page
			processTitle(processedTags, site);
			processHeaders(processedTags, site);
			processTexts(processedTags, site);

			// save site and gc
			db.save(site);
			result.putIfAbsent(site.getSiteID(), url);
			long siteID = site.getSiteID();
			site = null;

			// recursively crawl pages
			if (processedTags.containsKey(TagDataType.HTML_LINK)) {
				List<TagData> linkTags = processedTags.get(TagDataType.HTML_LINK);
				Phaser lowerLevelTracker = new Phaser(1);
				for (TagData td : linkTags) {

					Anchor anchor = saveAnchor(td, siteID);

					// check if limited to host
					if (limit2Host) {
						if (!anchor.getTargetURL().getHost().equals(url.getHost())) {
							continue;
						}
					}
					if (busyThreads.get() <= THREAD_COUNT) {
						busyThreads.incrementAndGet();
						lowerLevelTracker.register();
						executor.execute(new Runnable() {

							@Override
							public void run() {
								try {
									crawlRecursive(anchor.getTargetURL(), crawledSites, result);
								} finally {
									busyThreads.decrementAndGet();
									lowerLevelTracker.arriveAndDeregister();
								}
							}
						});
					} else {
						crawlRecursive(anchor.getTargetURL(), crawledSites, result);
					}
				}
				// wait for the threads to complete if there are any
				lowerLevelTracker.arriveAndAwaitAdvance();
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

	private Map<TagDataType, List<TagData>> recursiveInterpretTags(Element e, Site s) throws ParsingException {
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
			if (mime.contains("text/html") || mime.contains("text/htm") || mime.contains("text/plain")) {
				return true;
			}
			return false;
		} catch (Throwable e) {
			return false;
		} finally {
			urlc.disconnect();
		}
	}

	private boolean didCrawl(ConcurrentMap<URL, String> crawled, URL url) {
		return crawled.containsKey(url);
	}

	private boolean crawled(ConcurrentMap<URL, String> crawled, URL url, String hash) {
		return crawled.putIfAbsent(url, hash) == null;
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
				byte[] compressedHtm = compressor.compress(pageHtml.getBytes(StandardCharsets.UTF_16));

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

	private void processTitle(Map<TagDataType, List<TagData>> processedTags, Site site) {
		// parse title
		if (processedTags.containsKey(TagDataType.HTML_TITLE)) {
			site.setTitle(processedTags.get(TagDataType.HTML_TITLE).get(0).get("title", String.class));
		}
	}

	private Anchor saveAnchor(TagData anchorData, long siteID) throws IOException {
		URL linkedPage = anchorData.get("url", URL.class);
		String anchorName = anchorData.get("anchor", String.class);

		Anchor result = new Anchor();
		result.setAnchorText(anchorName);
		result.setTargetURL(linkedPage);
		result.setSiteID(siteID);
		anchorDB.save(result);
		return result;
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
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			executor.shutdownNow();
			return;
		}
	}

}
