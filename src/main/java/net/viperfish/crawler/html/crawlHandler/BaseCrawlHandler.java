package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;

public abstract class BaseCrawlHandler implements HttpCrawlerHandler {

	protected abstract boolean isParsed(CrawledData site);

	protected abstract boolean lock(CrawledData s);

	protected abstract boolean isFetched(URL url);

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		if (isFetched(content.getUrl().getToFetch())) {
			return HandlerResponse.HALT;
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
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
	public HandlerResponse handlePostProcess(CrawledData site) {
		return HandlerResponse.GO_AHEAD;
	}


}
