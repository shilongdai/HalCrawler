package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;

/**
 * A {@link net.viperfish.crawler.html.HttpCrawlerHandler} that defers page fetch with low priority
 * and kill page after a certain number of defer. This handler was created in order to try to ensure
 * that the most important pages are crawled first, and the deep linked pages with few reference are
 * abandoned to avoid crawler traps.
 */
public class TTLCrawlHandler extends YesCrawlChecker {

	private ConcurrentMap<URL, AtomicInteger> ttlTracker;
	private int priorityThreshold;
	private int deferredThreshold;

	/**
	 * creates a new {@link TTLCrawlHandler} specifying its minimal priority and its maximum
	 * deferred time.
	 *
	 * @param priorityThreshold the minimal required priority
	 * @param deferredThreshold the maximum deferred time.
	 */
	public TTLCrawlHandler(int priorityThreshold, int deferredThreshold) {
		this.priorityThreshold = priorityThreshold;
		ttlTracker = new ConcurrentHashMap<>();
		this.deferredThreshold = deferredThreshold;
	}

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		ttlTracker.putIfAbsent(content.getUrl().getToFetch(), new AtomicInteger(0));
		AtomicInteger current = ttlTracker.get(content.getUrl().getToFetch());
		if (content.getUrl().getPriority() - current.get() < priorityThreshold) {
			if (current.incrementAndGet() > deferredThreshold) {
				return HandlerResponse.HALT;
			}
			return HandlerResponse.DEFERRED;
		}
		return HandlerResponse.GO_AHEAD;
	}
}
