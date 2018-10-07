package net.viperfish.crawler.html.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
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
import net.viperfish.crawler.html.Restriction;
import net.viperfish.crawler.html.RestrictionManager;
import net.viperfish.crawler.html.exception.FetchFailedException;

public abstract class ConcurrentHttpFetcher implements HttpFetcher {

	private BlockingQueue<Pair<FetchedContent, Throwable>> queue;
	private ExecutorService threadPool;
	private AtomicInteger runningTasks;
	private List<RestrictionManager> managers;
	private String userAgent;
	private boolean closed;

	public ConcurrentHttpFetcher(int threadCount, String userAgent) {
		threadPool = Executors.newFixedThreadPool(threadCount);
		queue = new LinkedBlockingQueue<>();
		runningTasks = new AtomicInteger(0);
		this.managers = new LinkedList<>();
		closed = false;
		this.userAgent = userAgent;
	}

	public ConcurrentHttpFetcher(int threadCount) {
		this(threadCount, "halbot");
	}

	@Override
	public void submit(URL url) {
		runningTasks.incrementAndGet();
		threadPool.submit(new FetchRunnable(url));
	}

	@Override
	public FetchedContent next() throws FetchFailedException {
		try {
			Pair<FetchedContent, Throwable> result = queue.take();
			if (result.getSecond() != null) {
				throw new FetchFailedException(null, result.getFirst().getUrl());
			}
			return result.getFirst();
		} catch (InterruptedException e) {
			return null;
		}
	}

	@Override
	public FetchedContent next(long timeout, TimeUnit unit) throws FetchFailedException {
		try {
			Pair<FetchedContent, Throwable> result = queue.poll(timeout, unit);
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
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			threadPool.shutdownNow();
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

	protected ExecutorService getThreadPool() {
		return threadPool;
	}

	protected boolean closeCalled() {
		return closed;
	}

	protected BlockingQueue<Pair<FetchedContent, Throwable>> queue() {
		return queue;
	}

	private class FetchRunnable implements Runnable {

		private URL url;

		public FetchRunnable(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				for (RestrictionManager rm : managers) {
					Restriction restriction = rm.getRestriction(url);
					if (!restriction.canFetch()) {
						return;
					}
				}
				FetchedContent fetched = fetchSite(url);
				if (fetched != null) {
					queue.put(new Pair<>(fetched, null));
				}
			} catch (Throwable e) {
				queue.offer(new Pair<>(null, new FetchFailedException(e, url)));
			} finally {
				runningTasks.decrementAndGet();
			}
		}


		private FetchedContent fetchSite(URL url) throws IOException {
			URLConnection conn = url.openConnection();
			HttpURLConnection urlc;
			if (url.openConnection() instanceof HttpURLConnection) {
				// establish connection
				urlc = (HttpURLConnection) conn;
			} else {
				return null;
			}
			try {
				// fetch content
				urlc.setRequestMethod("GET");
				urlc.setRequestProperty("User-Agent", userAgent);
				urlc.connect();

				String mime = urlc.getContentType();
				if (mime == null) {
					return null;
				}
				boolean isHTML = mime.contains("text/html") || mime.contains("text/htm") || mime
					.contains("text/plain");
				if (!isHTML) {
					return null;
				}

				String pageHtml;
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
