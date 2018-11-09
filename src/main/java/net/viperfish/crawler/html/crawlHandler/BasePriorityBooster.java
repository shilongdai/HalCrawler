package net.viperfish.crawler.html.crawlHandler;

import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;

/**
 * A {@link net.viperfish.crawler.html.HttpCrawlerHandler} that boosts the priority of a fetched
 * site before processing. It will not affect the {@link net.viperfish.crawler.html.HttpFetcher}.
 * Thus, it should be used in conjuncture with the {@link TTLCrawlHandler}. This handler should be
 * registered before the {@link TTLCrawlHandler}.
 */
public abstract class BasePriorityBooster extends YesCrawlChecker {

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		content.getUrl().boostPriority(getBoostFactor(content));
		return HandlerResponse.GO_AHEAD;
	}

	/**
	 * get the boost factor for a specific site.
	 *
	 * @param content the fetched content representing the site.
	 * @return the boost factor for the site.
	 */
	protected abstract int getBoostFactor(FetchedContent content);
}
