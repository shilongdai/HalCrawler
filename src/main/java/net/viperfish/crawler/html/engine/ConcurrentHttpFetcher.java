package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.core.Pair;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HttpFetcher;
import net.viperfish.crawler.html.RestrictionManager;
import net.viperfish.crawler.html.exception.FetchFailedException;

public abstract class ConcurrentHttpFetcher implements HttpFetcher {

	// concurrency stuff
	private BlockingQueue<Pair<FetchedContent, Throwable>> resultQueue;
	private PrioritizedURLBlockingQueue prioritizedURLBlockingQueue;
	private ExecutorService threadPool;
	private ExecutorService delegatorThread;
	private AtomicInteger runningTasks;
	private Future<?> delegateInterrupter;

	// meta-info
	private List<RestrictionManager> managers;
	private String userAgent;
	private boolean closed;

	public ConcurrentHttpFetcher(int threadCount, String userAgent) {
		threadPool = Executors.newFixedThreadPool(threadCount);
		resultQueue = new LinkedBlockingQueue<>();
		runningTasks = new AtomicInteger(0);
		prioritizedURLBlockingQueue = new DefaultPrioritizedURLBlockingQueue();
		delegatorThread = Executors.newSingleThreadExecutor();

		this.managers = new LinkedList<>();
		closed = false;
		this.userAgent = userAgent;

		delegateInterrupter = delegatorThread.submit(new DelegatorRunnable());
	}

	public ConcurrentHttpFetcher(int threadCount) {
		this(threadCount, "halbot");
	}

	@Override
	public void submit(URL url) {
		prioritizedURLBlockingQueue.offer(url);
	}

	@Override
	public void submit(URL url, int priority) {
		prioritizedURLBlockingQueue.offer(url, priority);
	}

	@Override
	public FetchedContent next() throws FetchFailedException {
		try {
			Pair<FetchedContent, Throwable> result = resultQueue.take();
			if (result.getSecond() != null) {
				throw new FetchFailedException(null, result.getFirst().getUrl().getToFetch());
			}
			return result.getFirst();
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public FetchedContent next(long timeout, TimeUnit unit) throws FetchFailedException {
		try {
			Pair<FetchedContent, Throwable> result = resultQueue.poll(timeout, unit);
			if (result == null) {
				return null;
			}
			if (result.getSecond() != null) {
				if (result.getSecond() instanceof FetchFailedException) {
					throw (FetchFailedException) result.getSecond();
				}
				throw new FetchFailedException(result.getSecond());
			}
			return result.getFirst();
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public void close() {
		delegateInterrupter.cancel(true);
		delegatorThread.shutdown();
		threadPool.shutdown();
		try {
			delegatorThread.awaitTermination(5, TimeUnit.SECONDS);
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
			delegatorThread.shutdownNow();
		}
		closed = true;
	}

	@Override
	public void registerRestrictionManager(RestrictionManager mger) {
		this.managers.add(mger);
	}

	@Override
	public List<RestrictionManager> getRestrictionManagers() {
		return managers;
	}

	protected AtomicInteger getTaskNumber() {
		return runningTasks;
	}

	protected boolean closeCalled() {
		return closed;
	}

	protected BlockingQueue<Pair<FetchedContent, Throwable>> resultQueue() {
		return resultQueue;
	}

	protected PrioritizedURLBlockingQueue urlQueue() {
		return prioritizedURLBlockingQueue;
	}

	private class DelegatorRunnable implements Runnable {

		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					PrioritizedURL pURL = prioritizedURLBlockingQueue
						.take(200, TimeUnit.MILLISECONDS);
					if (pURL != null) {
						runningTasks.incrementAndGet();
						threadPool.submit(getRunnable(pURL));
					}
				}
			} catch (InterruptedException e) {
				return;
			}
		}

		private FetchRunnable getRunnable(PrioritizedURL url) {
			return new FetchRunnable(url, resultQueue, managers, runningTasks, userAgent);
		}
	}

}
