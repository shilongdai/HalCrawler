package net.viperfish.crawler.html.crawlHandler;

import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;
import net.viperfish.crawler.html.engine.PrioritizedURL;

/**
 * A {@link HttpCrawlerHandler} that ensures no further sites are crawled after the initial
 * submission has been crawled.
 */
public class NoCrawlChecker implements HttpCrawlerHandler {

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePreFetch(PrioritizedURL url) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePostProcess(CrawledData site) {
		return HandlerResponse.HALT;
	}

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		return HandlerResponse.GO_AHEAD;
	}
}
