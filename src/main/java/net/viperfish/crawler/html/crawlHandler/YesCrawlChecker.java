package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;

/**
 * A {@link HttpCrawlerHandler} that will always return an affirmative on crawling.
 */
public class YesCrawlChecker implements HttpCrawlerHandler {

	public YesCrawlChecker() {
	}

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePostProcess(CrawledData site) {
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		return HandlerResponse.GO_AHEAD;
	}
}
