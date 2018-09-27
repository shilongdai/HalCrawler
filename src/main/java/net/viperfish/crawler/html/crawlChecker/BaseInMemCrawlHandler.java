package net.viperfish.crawler.html.crawlChecker;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.viperfish.crawler.html.Site;

public class BaseInMemCrawlHandler extends BaseCrawlHandler {

	private ConcurrentMap<URL, Boolean> tracker;
	private ConcurrentMap<String, Boolean> hashTracker;

	public BaseInMemCrawlHandler() {
		tracker = new ConcurrentHashMap<>();
		hashTracker = new ConcurrentHashMap<>();
	}

	@Override
	protected boolean isParsed(Site site) {
		return isFetched(site.getUrl()) && hashTracker.containsKey(site.getChecksum());
	}

	@Override
	protected boolean isFetched(URL url) {
		return tracker.containsKey(url);
	}

	@Override
	protected boolean lock(Site s) {
		return tracker.putIfAbsent(s.getUrl(), true) == null
			&& hashTracker.putIfAbsent(s.getChecksum(), true) == null;
	}
}
