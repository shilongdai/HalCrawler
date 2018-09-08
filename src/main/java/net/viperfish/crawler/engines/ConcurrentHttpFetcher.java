package net.viperfish.crawler.engines;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import net.viperfish.crawler.base.FetchedContent;
import net.viperfish.crawler.base.HttpFetcher;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.core.Pair;

public class ConcurrentHttpFetcher implements HttpFetcher {

	private BlockingQueue<Pair<FetchedContent, Throwable>> queue;
	private ExecutorService threadPool;

	public ConcurrentHttpFetcher(int threadCount) {
		threadPool = Executors.newFixedThreadPool(threadCount);
		queue = new LinkedBlockingQueue<>();
	}

	@Override
	public void submit(URL url) {
		threadPool.submit(new FetchRunnable(url, queue));
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
	public void shutdown() {
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
		}
		return;
	}

	@Override
	public boolean isShutdown() {
		return threadPool.isShutdown();
	}

	private static class FetchRunnable implements Runnable {

		private URL url;
		private BlockingQueue<Pair<FetchedContent, Throwable>> queue;

		public FetchRunnable(URL url, BlockingQueue<Pair<FetchedContent, Throwable>> queue) {
			this.url = url;
			this.queue = queue;
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
