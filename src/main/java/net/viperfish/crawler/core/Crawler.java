package net.viperfish.crawler.core;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Crawler<I, O> {

	private ResourcesStream<? extends I> in;
	private Datasink<? super O> out;
	private ExecutorService masterDelegater;
	private Future<?> delegateTask;
	private ExecutorService processingThreads;
	private AtomicInteger activeProcessingThreads;

	public Crawler(ResourcesStream<? extends I> in, Datasink<? super O> out, int threads) {
		this.in = in;
		this.out = out;
		masterDelegater = Executors.newSingleThreadExecutor();
		processingThreads = Executors.newFixedThreadPool(threads);
		activeProcessingThreads = new AtomicInteger(0);
	}

	public void startCrawl() {
		if (delegateTask == null) {
			delegateTask = masterDelegater.submit(new Runnable() {
				@Override
				public void run() {
					while (!Thread.interrupted()) {
						if (in.isClosed() || in.isEndReached()) {
							return;
						}

						try {
							I next = in.next();

							// submit a new item to be concurrently processed
							activeProcessingThreads.incrementAndGet();
							processingThreads.submit(new Runnable() {
								@Override
								public void run() {
									try {
										O result = process(next);
										if (result != null) {
											out.write(result);
										}
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
										activeProcessingThreads.decrementAndGet();
									}
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}

	public boolean isDone() {
		if (delegateTask != null) {
			return delegateTask.isDone() && activeProcessingThreads.get() == 0;
		}
		return true;
	}

	public void waitUntiDone() throws InterruptedException {
		try {
			if (delegateTask != null) {
				delegateTask.get();
			}
			while (!isDone()) {
				Thread.sleep(50);
			}
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		return;
	}

	public void shutdown() throws IOException {
		try {
			in.close();
			out.close();
			masterDelegater.shutdown();
			processingThreads.shutdown();
			masterDelegater.awaitTermination(5, TimeUnit.SECONDS);
			processingThreads.awaitTermination(5, TimeUnit.SECONDS);
			masterDelegater.shutdownNow();
			processingThreads.shutdownNow();
		} catch (InterruptedException e) {
			masterDelegater.shutdownNow();
			processingThreads.shutdownNow();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}


	protected abstract O process(I input) throws Exception;

}