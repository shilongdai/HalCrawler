package net.viperfish.crawler.html;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.viperfish.crawler.core.ResourcesStream;
import net.viperfish.crawler.html.engine.PrioritizedURL;
import net.viperfish.crawler.html.exception.FetchFailedException;

/**
 * A {@link ResourcesStream} that fetches site contents with priorities as stream data.
 */
public interface HttpFetcher extends ResourcesStream<FetchedContent> {

	/**
	 * initializes this instance of the {@link HttpFetcher}.
	 */
	void init() throws Exception;

	/**
	 * submits a new url to be fetched. This URL will be checked against all
	 * constraints/restrictions of this fetcher. In terms of priority, this is the equivalent of
	 * calling submit(url, 0).
	 *
	 * @param url the url to fetch
	 */
	void submit(URL url);

	/**
	 * submits a new URL to be fetched with a certain priority. The URL with the highest priority
	 * will be fetched first.
	 *
	 * @param prioritizedURL the url with priority.
	 */
	void submit(PrioritizedURL prioritizedURL);

	/**
	 * sets the {@link RestrictionManager} that dictates whether the fetcher should fetch from a
	 * URL.
	 *
	 * @param mger the {@link RestrictionManager} that restricts the {@link HttpFetcher}.
	 */
	void registerRestrictionManager(RestrictionManager mger);

	/**
	 * gets the {@link RestrictionManager} currently in use by the class.
	 *
	 * @return the current {@link RestrictionManager} or null if none in use.
	 */
	List<RestrictionManager> getRestrictionManagers();

	@Override
	FetchedContent next() throws FetchFailedException;

	@Override
	FetchedContent next(long timeout, TimeUnit unit) throws FetchFailedException;
}
