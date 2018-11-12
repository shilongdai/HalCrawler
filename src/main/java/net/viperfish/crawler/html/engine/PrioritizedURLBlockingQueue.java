package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * A blocking url queue that ensures the most frequently added URL has the highest priority. The URL
 * with the higher frequency submitted should be retrieved earlier. All implementation of this class
 * must be thread safe.
 */
public interface PrioritizedURLBlockingQueue {

	/**
	 * offers a URL to the queue. This is an increase in the frequency of the URL, and the weight of
	 * the URL is adjusted accordingly.
	 *
	 * @param url the URL to add.
	 */
	void offer(URL url);

	/**
	 * offers a URL to the queue with a specified priority.
	 *
	 * @param prioritizedURL a url with priority.
	 */
	void offer(PrioritizedURL prioritizedURL);

	/**
	 * takes a URL from the resultQueue, blocking if no data available. This removes the URL. The
	 * URL with the higher frequency of submission is returned.
	 *
	 * @return the URL with high frequency of submission.
	 * @throws InterruptedException if the program is interrupted.
	 */
	PrioritizedURL take() throws InterruptedException;

	/**
	 * takes a URL from the resultQueue, blocking in respect to the time limited if no data
	 * available. This removes the URL. The URL with the higher frequency of submission is
	 * returned.
	 *
	 * @param time the timeout
	 * @param unit the unit for the timeout.
	 * @return the URL with high frequency of submission.
	 * @throws InterruptedException if the program is interrupted.
	 */
	PrioritizedURL take(int time, TimeUnit unit) throws InterruptedException;

	/**
	 * gets the size of the resultQueue.
	 *
	 * @return the size of the resultQueue.
	 */
	int size();
}
