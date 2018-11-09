package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger logger;

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
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		ttlTracker.putIfAbsent(content.getUrl().getToFetch(), new AtomicInteger(0));
		AtomicInteger current = ttlTracker.get(content.getUrl().getToFetch());
		logger.debug("Deferred count for {}: {}", content.getUrl().getToFetch().toExternalForm(),
			current.get());
		if (content.getUrl().getPriority() - current.get() < priorityThreshold) {
			if (current.incrementAndGet() > deferredThreshold) {
				logger.debug("{} exceeding TTL threshold {}, halting",
					content.getUrl().getToFetch().toExternalForm(), deferredThreshold);
				return HandlerResponse.HALT;
			}
			logger.debug("{} with priority {} under priority threshold {}, deferring",
				content.getUrl().getToFetch().toExternalForm(), content.getUrl().getPriority(),
				priorityThreshold);
			return HandlerResponse.DEFERRED;
		}
		return HandlerResponse.GO_AHEAD;
	}
}
