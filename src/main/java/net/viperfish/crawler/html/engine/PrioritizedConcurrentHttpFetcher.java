package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.core.Pair;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HttpFetcher;
import net.viperfish.crawler.html.RestrictionManager;
import net.viperfish.crawler.html.exception.FetchFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of the {@link HttpFetcher} that delegates fetch to threads. All
 * implementations of this base class need to provide the threading/concurrent mechanism.
 */
public abstract class PrioritizedConcurrentHttpFetcher implements HttpFetcher {

	// concurrency stuff
	private BlockingQueue<Pair<FetchedContent, Throwable>> resultQueue;
	private PrioritizedURLBlockingQueue prioritizedURLBlockingQueue;
	private AtomicInteger runningTasks;
	private Future<?> delegateInterrupter;

	// meta-info
	private List<RestrictionManager> managers;
	private String userAgent;
	private boolean closed;
	private Logger logger;

	/**
	 * creates a new {@link PrioritizedConcurrentHttpFetcher} with the user-agent string.
	 *
	 * @param userAgent the user-agent sent with the requests.
	 */
	public PrioritizedConcurrentHttpFetcher(String userAgent) {
		resultQueue = new LinkedBlockingQueue<>();
		runningTasks = new AtomicInteger(0);
		prioritizedURLBlockingQueue = new DefaultPrioritizedURLBlockingQueue();
		this.managers = new LinkedList<>();
		closed = false;
		this.userAgent = userAgent;
		logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public void init() {
		this.delegateInterrupter = runDelegator(new DelegatorRunnable());
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
		cleanup();
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

	/**
	 * gets the amount of fetching tasks that are submitted. This method is mostly for subclasses to
	 * determine when fetching is done.
	 *
	 * @return the amount of fetching tasks submitted.
	 */
	protected AtomicInteger getTaskNumber() {
		return runningTasks;
	}

	/**
	 * gets whether the close method is called. This method is mostly for subclasses.
	 *
	 * @return whether close is called.
	 */
	protected boolean closeCalled() {
		return closed;
	}

	/**
	 * gets the queue that contains all the fetch results.
	 *
	 * @return the queue with all the fetch result.
	 */
	protected BlockingQueue<Pair<FetchedContent, Throwable>> resultQueue() {
		return resultQueue;
	}

	/**
	 * gets the submission queue with urls to be processed.
	 *
	 * @return the queue with urls to be processed.
	 */
	protected PrioritizedURLBlockingQueue urlQueue() {
		return prioritizedURLBlockingQueue;
	}

	/**
	 * run the fetch task delegator concurrently.
	 *
	 * @param delegator the delegator runnable.
	 * @return a control point for the delegator.
	 */
	protected abstract Future<?> runDelegator(Runnable delegator);

	/**
	 * run a fetch task concurrently.
	 *
	 * @param fetcher the fetch task runnable.
	 * @return a control point for the fetch task.
	 */
	protected abstract Future<?> runFetcher(Runnable fetcher);

	/**
	 * clean up all resources used.
	 */
	protected abstract void cleanup();

	/**
	 * The delegator runnable task. It takes a url from the submission queue and delegate the
	 * fetching to {@link FetchRunnable}.
	 */
	private class DelegatorRunnable implements Runnable {

		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					PrioritizedURL pURL = prioritizedURLBlockingQueue
						.take(200, TimeUnit.MILLISECONDS);
					if (pURL != null) {
						runningTasks.incrementAndGet();
						logger.info("Going to fetch: {}", pURL.getToFetch());
						runFetcher(getRunnable(pURL));
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Interruption Received");
			}
		}

		/**
		 * shorthand for creating a {@link FetchRunnable}.
		 *
		 * @param url the url to fetch.
		 * @return a fetch runnable that will fetch the url.
		 */
		private FetchRunnable getRunnable(PrioritizedURL url) {
			return new FetchRunnable(url, resultQueue, managers, runningTasks, userAgent);
		}
	}

}
