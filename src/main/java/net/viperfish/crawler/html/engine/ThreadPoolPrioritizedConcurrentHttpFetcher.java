package net.viperfish.crawler.html.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public abstract class ThreadPoolPrioritizedConcurrentHttpFetcher extends
	PrioritizedConcurrentHttpFetcher {

	private int threadCount;
	private ExecutorService threadPool;

	public ThreadPoolPrioritizedConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(userAgent);
		this.threadCount = threadCount + 1;
		this.threadPool = Executors.newFixedThreadPool(this.threadCount);
	}

	@Override
	protected Future<?> runDelegator(Runnable delegator) {
		return threadPool.submit(delegator);
	}

	@Override
	protected Future<?> runFetcher(Runnable fetcher) {
		return threadPool.submit(fetcher);
	}

	@Override
	protected void shutdownThreads() {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		}
		threadPool.shutdownNow();

	}
}
