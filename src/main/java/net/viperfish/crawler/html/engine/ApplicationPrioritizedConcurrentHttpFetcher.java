package net.viperfish.crawler.html.engine;

/**
 * A {@link ThreadPoolPrioritizedConcurrentHttpFetcher} that is geared toward use with a standalone
 * application. This implementation is used in situation where the termination for crawling is clear
 * (i.e. a standalone web crawler application that downloads a website). Once there are no more
 * submission request and no more tasks running for fetching, this {@link
 * net.viperfish.crawler.html.HttpFetcher} is considered to reach its end.
 */
public class ApplicationPrioritizedConcurrentHttpFetcher extends
	ThreadPoolPrioritizedConcurrentHttpFetcher {

	/**
	 * creates a new application fetcher.
	 *
	 * @param threadCount the amount of thread for fetching.
	 * @param userAgent the user-agent sent to the servers.
	 */
	public ApplicationPrioritizedConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(threadCount, userAgent);
	}

	/**
	 * creates a new application fetcher with the default "halbot" user-agent.
	 *
	 * @param threadCount the amount of thread for fetching.
	 */
	public ApplicationPrioritizedConcurrentHttpFetcher(int threadCount) {
		super(threadCount, "halbot");
	}

	@Override
	public boolean isEndReached() {
		return resultQueue().size() == 0 && getTaskNumber().get() == 0 && urlQueue().size() == 0;
	}

	@Override
	public boolean isClosed() {
		return closeCalled();
	}
}
