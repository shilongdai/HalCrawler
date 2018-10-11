package net.viperfish.crawler.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A concurrent data processor that takes input from a {@link ResourcesStream} and output results to
 * a {@link Datasink}. It is the template base class for other classes that provide concrete
 * processing operations. All implementations of this class must be thread safe.
 */
public abstract class DataProcessor<I, O> {

	private ResourcesStream<? extends I> in;
	private Datasink<? super O> out;
	private ExecutorService masterDelegater;
	private Future<?> delegateTask;
	private ExecutorService processingThreads;
	private AtomicInteger activeProcessingTasks;

	/**
	 * creates a new {@link DataProcessor} with an input stream and an output stream. The object
	 * will spawn the specified amounts of thread to process informations.
	 */
	public DataProcessor(ResourcesStream<? extends I> in, Datasink<? super O> out, int threads) {
		this.in = in;
		this.out = out;
		masterDelegater = Executors.newSingleThreadExecutor();
		processingThreads = Executors.newFixedThreadPool(threads);
		activeProcessingTasks = new AtomicInteger(0);
	}

	/**
	 * starts to pull data from the {@link ResourcesStream} and process it to the {@link Datasink}
	 * concurrently. This method will not block.
	 */
	public void startProcessing() {
		if (delegateTask == null) {
			delegateTask = masterDelegater.submit(new Runnable() {
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
							processingThreads.submit(new Runnable() {
								@Override
								public void run() {
									try {
										ProcessedResult<O> result = process(next);
										if (result != null) {
											if (result.shouldOutput()) {
												out.write(result.getResult());
											}
										}
									} catch (Exception e) {
										handleProcessingError(e);
									} finally {
										activeProcessingTasks.decrementAndGet();
									}
								}
							});
						} catch (Exception e) {
							processFetchError(e);
						}
					}
				}
			});
		}
	}

	/**
	 * tests whether all the data from the stream have been processed.
	 *
	 * @return if data from the stream have been processed.
	 */
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
	 * resets the {@link DataProcessor} to start processing again. This method will only reset the
	 * {@link DataProcessor}, and will not touch the {@link ResourcesStream} or the {@link
	 * Datasink}.
	 */
	public void reset() {
		delegateTask.cancel(true);
		delegateTask = null;
		activeProcessingTasks = new AtomicInteger(0);
	}

	/**
	 * shuts down all processing operations and clean up. This method will not touch the {@link
	 * ResourcesStream} or the {@link Datasink}.
	 */
	public void shutdown() {
		try {
			masterDelegater.shutdown();
			processingThreads.shutdown();
			masterDelegater.awaitTermination(5, TimeUnit.SECONDS);
			processingThreads.awaitTermination(5, TimeUnit.SECONDS);
			masterDelegater.shutdownNow();
			processingThreads.shutdownNow();
		} catch (InterruptedException e) {
			masterDelegater.shutdownNow();
			processingThreads.shutdownNow();
		}
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
	 * handles an error that occurred when fetching an item from the {@link ResourcesStream}. This method should be thread safe.
	 * @param e the exception that occurred.
	 */
	protected void processFetchError(Exception e) {
		System.out.println("Failed to read:" + e.getMessage());
	}

	/**
	 * handles an error that occurred during the processing phase. This method should be thread
	 * safe.
	 *
	 * @param e the exception that occurred.
	 */
	protected void handleProcessingError(Exception e) {
		System.out.println("Failed to handle:" + e.getMessage());
	}

}
