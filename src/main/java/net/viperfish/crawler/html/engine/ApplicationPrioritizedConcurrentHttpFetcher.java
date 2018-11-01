package net.viperfish.crawler.html.engine;

public class ApplicationPrioritizedConcurrentHttpFetcher extends
	ThreadPoolPrioritizedConcurrentHttpFetcher {

	public ApplicationPrioritizedConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(threadCount, userAgent);
	}

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
