package net.viperfish.crawler.html.engine;

/**
 * A {@link ThreadPoolPrioritizedConcurrentHttpFetcher} geared towards a service context. The use
 * case for this implementation is a long running service with crawling capability. The {@link
 * net.viperfish.crawler.html.HttpFetcher} will not reach the end of stream until the close method
 * is called.
 */
public class ServicePrioritizedConcurrentHttpFetcher extends
	ThreadPoolPrioritizedConcurrentHttpFetcher {

	/**
	 * creates a new fetcher with specified thread count and user-agent.
	 *
	 * @param threadCount the amount of thread for fetching.
	 * @param userAgent the user-agent sent to servers.
	 */
	public ServicePrioritizedConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(threadCount, userAgent);
	}

	/**
	 * creates a new fetcher with default user-agent "halbot" and a specified number of fetching
	 * thread.
	 *
	 * @param threadCount the amount of thread for fetching.
	 */
	public ServicePrioritizedConcurrentHttpFetcher(int threadCount) {
		super(threadCount, "halbot");
	}

	@Override
	public boolean isEndReached() {
		return isClosed();
	}

	@Override
	public boolean isClosed() {
		return closeCalled();
	}
}
