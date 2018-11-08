package net.viperfish.crawler.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A concurrent data processor that takes input from a {@link ResourcesStream} and output results to
 * a {@link Datasink}. It is the template base class for other classes that provide concrete
 * processing operations. All implementations of this class must be thread safe.
 */
public abstract class ConcurrentDataProcessor<I, O> implements Processor {

	private ResourcesStream<? extends I> in;
	private Datasink<? super O> out;
	private Future<?> delegateTask;
	private AtomicInteger activeProcessingTasks;

	/**
	 * creates a new {@link ConcurrentDataProcessor} with an input stream and an output stream. The
	 * object will spawn the specified amounts of thread to process informations.
	 */
	public ConcurrentDataProcessor(ResourcesStream<? extends I> in, Datasink<? super O> out) {
		this.in = in;
		this.out = out;
		activeProcessingTasks = new AtomicInteger(0);
	}

	/**
	 * starts to pull data from the {@link ResourcesStream} and process it to the {@link Datasink}
	 * concurrently. This method will not block.
	 */
	@Override
	public void startProcessing() {
		if (delegateTask == null) {
			delegateTask = this.runDelegator(new Delegator());
		}
	}

	/**
	 * tests whether all the data from the stream have been processed.
	 *
	 * @return if data from the stream have been processed.
	 */
	@Override
	public boolean isDone() {
		if (delegateTask != null) {
			return delegateTask.isDone() && activeProcessingTasks.get() == 0;
		}
		return true;
	}

	/**
	 * blocks until all the data from the stream have been processed.
	 *
	 * @throws InterruptedException if an interruption occurs.
	 */
	@Override
	public void waitUntiDone() throws InterruptedException {
		try {
			if (delegateTask != null) {
				delegateTask.get();
			}
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * resets the {@link ConcurrentDataProcessor} to start processing again. This method will only
	 * reset the {@link ConcurrentDataProcessor}, and will not touch the {@link ResourcesStream} or
	 * the {@link Datasink}.
	 */
	@Override
	public void reset() {
		delegateTask.cancel(true);
		delegateTask = null;
		activeProcessingTasks = new AtomicInteger(0);
	}

	/**
	 * shuts down all processing operations and clean up. This method will not touch the {@link
	 * ResourcesStream} or the {@link Datasink}.
	 */
	@Override
	public void shutdown() {
		if (delegateTask != null) {
			delegateTask.cancel(true);
		}
		cleanup();
	}

	/**
	 * processes an item from the input stream. This is the template method for this class. The
	 * calls to this method will be concurrent, so the implementation should ensure thread safety.
	 *
	 * @param input the pulled item from the stream.
	 * @return the result of the processing.
	 * @throws Exception if any error occurred during the processing.
	 */
	protected abstract ProcessedResult<O> process(I input) throws Exception;

	/**
	 * handles an error that occurred when fetching an item from the {@link ResourcesStream}. This
	 * method should be thread safe.
	 *
	 * @param e the exception that occurred.
	 */
	protected void processFetchError(Throwable e) {
		System.out.println("Failed to read:" + e.getMessage());
	}

	/**
	 * handles an error that occurred during the processing phase. This method should be thread
	 * safe.
	 *
	 * @param e the exception that occurred.
	 */
	protected void handleProcessingError(Throwable e) {
		System.out.println("Failed to handle:" + e.getMessage());
	}

	/**
	 * runs the delegator concurrently. This will only be called for once in a given instance.
	 *
	 * @param delegatorTask the delegator.
	 * @return a control point.
	 */
	protected abstract Future<?> runDelegator(Runnable delegatorTask);

	/**
	 * runs a processing task concurrently. This will be called for every incoming item from the
	 * queue.
	 *
	 * @return a control point.
	 */
	protected abstract Future<?> runProcessor(Runnable processor);

	/**
	 * cleans up all resources used by the subclasses.
	 */
	protected abstract void cleanup();

	/**
	 * A delegator Runnable that is responsible for dispatching incoming fetched sites to different
	 * threads in the form of the {@link Processor}.
	 */
	private class Delegator implements Runnable {

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				// exit if there are no data left and that no processing are being done.
				if ((in.isClosed() || in.isEndReached())
					&& activeProcessingTasks.get() == 0) {
					return;
				}

				try {
					I next = in.next(100, TimeUnit.MILLISECONDS);
					if (next == null) {
						continue;
					}

					// submit a new item to be concurrently processed
					activeProcessingTasks.incrementAndGet();
					runProcessor(new Processor(next));
				} catch (Throwable e) {
					processFetchError(e);
				}
			}
		}
	}

	/**
	 * The processing unit that accepts a crawled site, processes it, and write the result to the
	 * {@link Datasink}.
	 */
	private class Processor implements Runnable {

		private I next;

		public Processor(I next) {
			this.next = next;
		}

		@Override
		public void run() {
			try {
				ProcessedResult<O> result = process(next);
				if (result != null) {
					if (result.shouldOutput()) {
						out.write(result.getResult());
					}
				}
			} catch (Throwable e) {
				handleProcessingError(e);
			} finally {
				activeProcessingTasks.decrementAndGet();
			}
		}
	}

}
