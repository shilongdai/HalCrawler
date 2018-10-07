package net.viperfish.crawler.html.engine;

public class ServiceConcurrentHttpFetcher extends ConcurrentHttpFetcher {

	public ServiceConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(threadCount, userAgent);
	}

	public ServiceConcurrentHttpFetcher(int threadCount) {
		super(threadCount);
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
