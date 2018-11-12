package net.viperfish.crawler.html;

import net.viperfish.crawler.html.engine.PrioritizedURL;

/**
 * A handler that can be attached to the {@link HttpWebCrawler} to perform operations at various
 * stages of processing. Additionally, it can also control the flow of processing by returning
 * {@link HandlerResponse}. All implementations of this class should ensure thread safety.
 */
public interface HttpCrawlerHandler {

	/**
	 * performs an operation or control flow before the initial parse. At this stage, only the
	 * fetched contents are available.
	 *
	 * @param content the fetched content.
	 * @return the control signal.
	 */
	HandlerResponse handlePreParse(FetchedContent content);

	/**
	 * perform operation or control flow after the initial parse. At this stage, all the built-in
	 * fields of the {@link CrawledData} are filled in.
	 *
	 * @param site the {@link CrawledData} with built-in fields filled in.
	 * @return the control signal.
	 */
	HandlerResponse handlePostParse(CrawledData site);

	/**
	 * performs operation or control flow before the site is fetched. At this stage, only the URL is
	 * available.
	 *
	 * @param url the URL about to be fetched.
	 * @return the control signal.
	 */
	HandlerResponse handlePreFetch(PrioritizedURL url);

	/**
	 * performs operation or control flow after all the {@link TagProcessor}s are ran. This is ran
	 * after the handlePostParse method.
	 *
	 * @param site the processed site.
	 * @return the control signal.
	 */
	HandlerResponse handlePostProcess(CrawledData site);
}
