package net.viperfish.crawler.html.engine;

public class ServicePrioritizedConcurrentHttpFetcher extends
	ThreadPoolPrioritizedConcurrentHttpFetcher {

	public ServicePrioritizedConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(threadCount, userAgent);
	}

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
