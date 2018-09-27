package net.viperfish.crawler.html.crawlChecker;

import java.net.URL;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;
import net.viperfish.crawler.html.Site;

public abstract class BaseCrawlHandler implements HttpCrawlerHandler {

	protected abstract boolean isParsed(Site site);

	protected abstract boolean lock(Site s);

	protected abstract boolean isFetched(URL url);

	@Override
	public HandlerResponse handlePostParse(Site site) {
		boolean isParsed = isParsed(site);
		if (isParsed) {
			return HandlerResponse.HALT;
		}
		boolean lock = lock(site);
		if (!lock) {
			return HandlerResponse.HALT;
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		boolean isFetched = isFetched(url);
		if (isFetched) {
			return HandlerResponse.HALT;
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePostProcess(Site site) {
		return HandlerResponse.GO_AHEAD;
	}
}
