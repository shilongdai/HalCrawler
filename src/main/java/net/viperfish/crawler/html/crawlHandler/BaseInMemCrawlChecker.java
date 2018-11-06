package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.viperfish.crawler.html.CrawledData;

/**
 * A basic implementation of the {@link BaseCrawlChecker} that keeps all the data in memory.
 */
public class BaseInMemCrawlChecker extends BaseCrawlChecker {

	private ConcurrentMap<URL, Boolean> tracker;
	private ConcurrentMap<String, Boolean> hashTracker;

	/**
	 * creates a new {@link BaseInMemCrawlChecker}.
	 */
	public BaseInMemCrawlChecker() {
		tracker = new ConcurrentHashMap<>();
		hashTracker = new ConcurrentHashMap<>();
	}

	@Override
	protected boolean isParsed(CrawledData site) {
		return isFetched(site.getUrl()) && hashTracker.containsKey(site.getChecksum());
	}

	@Override
	protected boolean isFetched(URL url) {
		return tracker.containsKey(url);
	}

	@Override
	protected boolean lock(CrawledData s) {
		return tracker.putIfAbsent(s.getUrl(), true) == null
			&& hashTracker.putIfAbsent(s.getChecksum(), true) == null;
	}

	protected ConcurrentMap<URL, Boolean> getURLTracker() {
		return this.tracker;
	}

	protected ConcurrentMap<String, Boolean> getHashTracker() {
		return this.hashTracker;
	}
}
