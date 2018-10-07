package net.viperfish.crawler.html;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.viperfish.crawler.core.ResourcesStream;
import net.viperfish.crawler.html.exception.FetchFailedException;

/**
 * A {@link ResourcesStream} that fetches site contents as stream data.
 */
public interface HttpFetcher extends ResourcesStream<FetchedContent> {

	/**
	 * submits a new url to be fetched.
	 *
	 * @param url the url to fetch
	 */
	void submit(URL url);

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

	/**
	 * checks if:
	 * <ol>
	 * <li>if the fetcher is currently fetching</li>
	 * <li>if there are still urls to be fetched</li>
	 * </ol>
	 *
	 * @return true if none of the conditions above are met, false otherwise.
	 */
	@Override
	boolean isEndReached();

	@Override
	FetchedContent next() throws FetchFailedException;

	@Override
	FetchedContent next(long timeout, TimeUnit unit) throws FetchFailedException;
}
