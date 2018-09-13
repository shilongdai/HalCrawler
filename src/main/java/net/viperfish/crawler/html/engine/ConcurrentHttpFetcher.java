package net.viperfish.crawler.html.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.core.Pair;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HttpFetcher;

public class ConcurrentHttpFetcher implements HttpFetcher {

	private BlockingQueue<Pair<FetchedContent, Throwable>> queue;
	private ExecutorService threadPool;
	private AtomicInteger runningTasks;

	public ConcurrentHttpFetcher(int threadCount) {
		threadPool = Executors.newFixedThreadPool(threadCount);
		queue = new LinkedBlockingQueue<>();
		runningTasks = new AtomicInteger(0);
	}

	@Override
	public void submit(URL url) {
		runningTasks.incrementAndGet();
		threadPool.submit(new FetchRunnable(url));
	}

	@Override
	public FetchedContent next() throws IOException {
		try {
			Pair<FetchedContent, Throwable> result = queue.take();
			if (result.getSecond() != null) {
				throw new IOException(result.getSecond());
			}
			return result.getFirst();
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public FetchedContent next(long timeout, TimeUnit unit) throws IOException {
		try {
			Pair<FetchedContent, Throwable> result = queue.poll(timeout, unit);
			if (result == null) {
				return null;
			}
			if (result.getSecond() != null) {
				throw new IOException(result.getSecond());
			}
			return result.getFirst();
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public void close() {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
		}
		return;
	}

	@Override
	public boolean isClosed() {
		return threadPool.isShutdown();
	}

	@Override
	public boolean isEndReached() {
		return queue.size() == 0 && runningTasks.get() == 0;
	}


	private class FetchRunnable implements Runnable {

		private URL url;

		public FetchRunnable(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				FetchedContent fetched = fetchSite(url);
				if (fetched != null) {
					queue.put(new Pair<>(fetched, null));
				}
			} catch (Throwable e) {
				queue.offer(new Pair<>(null, e));
				return;
			} finally {
				runningTasks.decrementAndGet();
			}
		}


		private FetchedContent fetchSite(URL url) throws IOException {
			URLConnection conn = url.openConnection();
			HttpURLConnection urlc = null;
			if (url.openConnection() instanceof HttpURLConnection) {
				// establish connection
				urlc = (HttpURLConnection) conn;
			} else {
				return null;
			}
			try {
				// fetch content
				urlc.setRequestMethod("GET");
				urlc.connect();

				String mime = urlc.getContentType();
				boolean isHTML = mime.contains("text/html") || mime.contains("text/htm") || mime
					.contains("text/plain");
				if (!isHTML) {
					return null;
				}

				String pageHtml = null;
				try {
					pageHtml = new String(IOUtil.read(urlc.getInputStream()),
						getEncoding(urlc));
				} catch (IOException e) {
					pageHtml = "";
				}
				return new FetchedContent(url, urlc.getResponseCode(), pageHtml);
			} finally {
				if (urlc != null) {
					urlc.disconnect();
				}
			}
		}

		private String getEncoding(HttpURLConnection urlConnection) {
			String mime = urlConnection.getContentType();
			String encoding = urlConnection.getContentEncoding();
			if (encoding != null) {
				return encoding;
			}

			int charsetIndex = mime.lastIndexOf("charset");
			if (charsetIndex != -1) {
				int encodingIndex = charsetIndex + 8;
				return mime.substring(encodingIndex).trim();
			}

			return "UTF-8";
		}
	}
}
