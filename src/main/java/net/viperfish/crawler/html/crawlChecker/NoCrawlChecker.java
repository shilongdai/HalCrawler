package net.viperfish.crawler.html.crawlChecker;

import java.net.URL;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;
import net.viperfish.crawler.html.Site;

public class NoCrawlChecker implements HttpCrawlerHandler {

	@Override
	public HandlerResponse handlePostParse(Site site) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePostProcess(Site site) {
		return HandlerResponse.HALT;
	}
}
