package net.viperfish.crawler.html;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import net.viperfish.crawler.core.Datasink;

public class ThreadPoolHttpWebCrawler extends HttpWebCrawler {

	private ExecutorService threadpool;

	public ThreadPoolHttpWebCrawler(int threadCount,
		Datasink<CrawledData> db,
		HttpFetcher fetcher) {
		super(db, fetcher);
		threadpool = Executors.newFixedThreadPool(threadCount + 1);
	}

	@Override
	protected Future<?> runDelegator(Runnable delegatorTask) {
		return threadpool.submit(delegatorTask);
	}

	@Override
	protected Future<?> runProcessor(Runnable processor) {
		return threadpool.submit(processor);
	}

	@Override
	protected void cleanup() {
		threadpool.shutdown();
		try {
			threadpool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			threadpool.shutdownNow();
		}
		threadpool.shutdownNow();
	}
}
