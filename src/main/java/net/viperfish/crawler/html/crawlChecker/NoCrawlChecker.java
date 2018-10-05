package net.viperfish.crawler.html.crawlChecker;

import java.net.URL;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;

public class NoCrawlChecker implements HttpCrawlerHandler {

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePostProcess(CrawledData site) {
		return HandlerResponse.HALT;
	}
}