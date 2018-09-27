package net.viperfish.crawler.html.crawlChecker;

import java.net.URL;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;
import net.viperfish.crawler.html.Site;

public final class YesCrawlChecker implements HttpCrawlerHandler {

	public YesCrawlChecker() {
	}

	@Override
	public HandlerResponse handlePostParse(Site site) {
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePostProcess(Site site) {
		return HandlerResponse.GO_AHEAD;
	}
}
