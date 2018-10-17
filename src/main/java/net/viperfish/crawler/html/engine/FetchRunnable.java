package net.viperfish.crawler.html.engine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.core.Pair;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.Restriction;
import net.viperfish.crawler.html.RestrictionManager;
import net.viperfish.crawler.html.exception.FetchFailedException;

class FetchRunnable implements Runnable {

	private PrioritizedURL url;
	private BlockingQueue<Pair<FetchedContent, Throwable>> queue;
	private List<RestrictionManager> managers;
	private AtomicInteger runningTasks;
	private String userAgent;

	public FetchRunnable(PrioritizedURL url,
		BlockingQueue<Pair<FetchedContent, Throwable>> queue,
		List<RestrictionManager> managers, AtomicInteger runningTasks, String userAgent) {
		this.url = url;
		this.queue = queue;
		this.managers = managers;
		this.runningTasks = runningTasks;
		this.userAgent = userAgent;
	}

	@Override
	public void run() {
		try {
			for (RestrictionManager rm : managers) {
				Restriction restriction = rm.getRestriction(url.getToFetch());
				if (!restriction.canFetch()) {
					return;
				}
			}
			FetchedContent fetched = fetchSite(url);
			if (fetched != null) {
				queue.put(new Pair<>(fetched, null));
			}
		} catch (Throwable e) {
			queue.offer(new Pair<>(null, new FetchFailedException(e, url.getToFetch())));
		} finally {
			runningTasks.decrementAndGet();
		}
	}


	private FetchedContent fetchSite(PrioritizedURL url) throws IOException {
		URLConnection conn = url.getToFetch().openConnection();
		HttpURLConnection urlc;
		if (url.getToFetch().openConnection() instanceof HttpURLConnection) {
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