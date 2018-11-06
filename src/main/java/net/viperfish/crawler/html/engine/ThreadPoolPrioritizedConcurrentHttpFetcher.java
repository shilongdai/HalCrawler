package net.viperfish.crawler.html.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A {@link PrioritizedConcurrentHttpFetcher} that uses a self managed thread pool as the
 * concurrency mechanism.
 */
public abstract class ThreadPoolPrioritizedConcurrentHttpFetcher extends
	PrioritizedConcurrentHttpFetcher {

	private int threadCount;
	private ExecutorService threadPool;

	/**
	 * creates a new fetcher.
	 *
	 * @param threadCount the amount of thread used for fetching.
	 * @param userAgent the user-agent sent to the servers.
	 */
	public ThreadPoolPrioritizedConcurrentHttpFetcher(int threadCount, String userAgent) {
		super(userAgent);
		this.threadCount = threadCount + 1;
		this.threadPool = Executors.newFixedThreadPool(this.threadCount);
	}

	/**
	 * run the delegator task with the thread pool.
	 *
	 * @param delegator the delegator runnable.
	 * @return the control point from the thread pool.
	 */
	@Override
	protected Future<?> runDelegator(Runnable delegator) {
		return threadPool.submit(delegator);
	}

	/**
	 * run the fetcher with the thread pool.
	 *
	 * @param fetcher the fetch task runnable.
	 * @return the control point for fetcher.
	 */
	@Override
	protected Future<?> runFetcher(Runnable fetcher) {
		return threadPool.submit(fetcher);
	}

	/**
	 * shuts down the thread pool.
	 */
	@Override
	protected void cleanup() {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.out.println("Interruption received");
		}
		threadPool.shutdownNow();

	}
}
