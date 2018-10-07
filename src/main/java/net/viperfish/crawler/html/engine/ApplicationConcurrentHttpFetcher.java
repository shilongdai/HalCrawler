package net.viperfish.crawler.html.engine;

public class ApplicationConcurrentHttpFetcher extends ConcurrentHttpFetcher {

	public ApplicationConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(threadCount, userAgent);
	}

	public ApplicationConcurrentHttpFetcher(int threadCount) {
		super(threadCount);
	}

	@Override
	public boolean isEndReached() {
		return queue().size() == 0 && getTaskNumber().get() == 0;
	}

	@Override
	public boolean isClosed() {
		return closeCalled();
	}
}
