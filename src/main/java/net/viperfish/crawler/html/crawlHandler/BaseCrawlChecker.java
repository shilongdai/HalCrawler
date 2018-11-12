package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.HttpCrawlerHandler;
import net.viperfish.crawler.html.engine.PrioritizedURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base implementation of a {@link HttpCrawlerHandler} that ensures all pages are only crawled
 * once. It can be seen as a locking mechanism that locks a page after it is crawled or while it is
 * being crawled. It is expected that this class would be called across multiple threads. So, all
 * implementations of this class must be thread safe.
 */
public abstract class BaseCrawlChecker implements HttpCrawlerHandler {

	private Logger logger;

	public BaseCrawlChecker() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * checks if the site has already been processed.
	 *
	 * @return true if the page hash already been processed.
	 */
	protected abstract boolean isParsed(CrawledData site);

	/**
	 * locks the page while a page is being processed.
	 *
	 * @param s the page being processed.
	 * @return true if successfully locked.
	 */
	protected abstract boolean lock(CrawledData s);

	/**
	 * checks if a URL has been or is being processed before it is submitted to fetching.
	 *
	 * @param url the url to check.
	 * @return true if the url is being processed.
	 */
	protected abstract boolean isFetched(URL url);

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		logger.debug("PreParse: Checking {} against fetched:",
			content.getUrl().getSource().toExternalForm());
		if (isFetched(content.getUrl().getSource())) {
			logger.debug("PreParse: {} already fetched, halting.", content.getUrl().getSource());
			return HandlerResponse.HALT;
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
		logger.debug("PostParse: Checking if {} is already in progress",
			site.getUrl().toExternalForm());
		boolean isParsed = isParsed(site);
		if (isParsed) {
			logger.debug("PostParse: {} is already in progress, halting",
				site.getUrl().toExternalForm());
			return HandlerResponse.HALT;
		}
		logger.debug("PostParse: Attempting to lock {}", site.getUrl().toExternalForm());
		boolean lock = lock(site);
		if (!lock) {
			logger.debug("PostParse: Locking failed, halting");
			return HandlerResponse.HALT;
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreFetch(PrioritizedURL url) {
		logger.debug("PreFetch: Checking if {} is fetched", url.getSource().toExternalForm());
		boolean isFetched = isFetched(url.getSource());
		if (isFetched) {
			logger.debug("PreFetch: {} already fetched, halting", url.getSource().toExternalForm());
			return HandlerResponse.HALT;
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePostProcess(CrawledData site) {
		return HandlerResponse.GO_AHEAD;
	}


}
