package net.viperfish.crawler.html.engine;

import net.viperfish.crawler.html.RestrictionManager;

public class ServiceConcurrentHttpFetcher extends ConcurrentHttpFetcher {

	public ServiceConcurrentHttpFetcher(int threadCount,
		RestrictionManager manager, String userAgent) {
		super(threadCount, manager, userAgent);
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
