package net.viperfish.crawler.base;

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
	private CrawlChecker crawlChecker;
	private BlockingQueue<Long> results;
	private AtomicInteger activeThreads;
	private HttpFetcher fetcher;

	public BaseHttpWebCrawler(SiteDatabase db, DatabaseObject<Long, Anchor> anchorDB,
		HttpFetcher fetcher) {
		processors = new HashMap<>();
		this.fetcher = fetcher;
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
		results = new LinkedBlockingQueue<>();
		activeThreads = new AtomicInteger(0);

		startCrawlers();
	}

	@Override
	public void submit(URL url) {
		fetcher.submit(url);
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
		fetcher.shutdown();
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

	private void crawlIterative() {
		try {
			// parse the content of site
			FetchedContent content = fetcher.next();
			if (!ACCEPTED_STATUS_CODE.contains(content.getStatus())) {
				return;
			}
			Site site = toSite(content);
			// make sure not repeated
			if (!crawlChecker.shouldCrawl(content.getUrl(), site)) {
				return;
			}
			if (!crawlChecker.lock(content.getUrl(), site)) {
				return;
			}
			String cleanHTML = Jsoup.clean(content.getRawHTML(), content.getUrl().toExternalForm(),
				Whitelist.relaxed().addTags("title").addTags("head"));
			Document doc = Jsoup.parse(cleanHTML);

			// get and process all of the tags
			Map<TagDataType, List<TagData>> processedTags = recursiveInterpretTags(
				doc.getElementsByTag("html").first(), site);

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
						if (!anchor.getTargetURL().getHost().equals(content.getUrl().getHost())) {
							continue;
						}
					}

					// make sure not repeating
					if (crawlChecker.shouldCrawl(anchor.getTargetURL())) {
						fetcher.submit(anchor.getTargetURL());
					}
				}
				anchorDB.save(anchors);
			}
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
	}

	private Site toSite(FetchedContent content) {
		byte[] compressed = this.compressor
			.compress(content.getRawHTML().getBytes(StandardCharsets.UTF_8));
		String checksum = hashSite(content.getRawHTML());

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
		TagProcessor processor = processors.get(e.tagName());
		if (processor != null) {
			if (processor.shouldProcess(e)) {
				result = processor.processTag(e, s);
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
					while (!Thread.interrupted() && !fetcher.isShutdown()) {
						try {
							activeThreads.incrementAndGet();
							crawlIterative();
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
