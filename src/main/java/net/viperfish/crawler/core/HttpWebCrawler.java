package net.viperfish.crawler.core;

import java.net.URL;

/**
 * A web spider that crawls the pages and linked pages on the Internet. All implementations of this interface must ensure the thread safety of the class. All crawled sites should be stored in some kind of {@link SiteDatabase}.
 */
public interface HttpWebCrawler {

	/**
	 * submits a starting URL to be crawled.
	 * @param url
	 */
	void submit(URL url);

	/**
	 * makes the crawler limit the crawling to only the site indicated in the {@link HttpWebCrawler#submit(URL)}.
	 * @param limit <code>true</code> for limit <code>false</code> for no limit.
	 */
	void limitToHost(boolean limit);

	/**
	 * check if the crawler is limited to the url indicated in {@link HttpWebCrawler#submit(URL)}.
	 * @return <code>true</code>  if limited to the explicitly specified sites, <code>false</code> otherwise.
	 */
	boolean isLimitedToHost();

	/**
	 * Terminates the crawler and release any resources used.
	 */
	void shutdown();

	/**
	 * checks if the crawler is currently crawling or not.
	 * @return <code>true</code> if the crawler is <b>not</b> crawling, <code>false</code> otherwise.
	 */
	boolean isIdle();
}
