package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import net.viperfish.crawler.html.HandlerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A crawl handler that ensures only the pages from a certain host is crawled after the initial
 * submission to the {@link net.viperfish.crawler.html.HttpWebCrawler}.
 */
public class Limit2HostHandler extends YesCrawlChecker {

	private String host;
	private Logger logger;

	/**
	 * creates a new {@link Limit2HostHandler} specifying the limited host.
	 *
	 * @param host the host of the limit.
	 */
	public Limit2HostHandler(String host) {
		this.host = host;
		logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		logger.debug("Checking {} against {}", url.getHost(), host);
		if (url.getHost().equals(host)) {
			return HandlerResponse.GO_AHEAD;
		}
		logger.debug("Host does not match, halting");
		return HandlerResponse.HALT;
	}
}
