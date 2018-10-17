package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;

public class TTLCrawlHandler extends YesCrawlChecker {

	private ConcurrentMap<URL, AtomicInteger> ttlTracker;
	private int ttlThreshold;
	private int deferredThreshold;


	public TTLCrawlHandler(int ttlThreshold, int deferredThreshold) {
		this.ttlThreshold = ttlThreshold;
		ttlTracker = new ConcurrentHashMap<>();
		this.deferredThreshold = deferredThreshold;
	}

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		ttlTracker.putIfAbsent(content.getUrl().getToFetch(), new AtomicInteger(0));
		AtomicInteger current = ttlTracker.get(content.getUrl().getToFetch());
		if (content.getUrl().getPriority() - current.get() < ttlThreshold) {
			if (current.incrementAndGet() > deferredThreshold) {
				return HandlerResponse.HALT;
			}
			return HandlerResponse.DEFERRED;
		}
		return HandlerResponse.GO_AHEAD;
	}
}
