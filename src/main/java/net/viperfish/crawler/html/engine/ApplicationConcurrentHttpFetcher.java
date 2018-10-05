package net.viperfish.crawler.html.engine;

import net.viperfish.crawler.html.RestrictionManager;

public class ApplicationConcurrentHttpFetcher extends ConcurrentHttpFetcher {

	public ApplicationConcurrentHttpFetcher(int threadCount,
		RestrictionManager manager) {
		super(threadCount, manager);
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
