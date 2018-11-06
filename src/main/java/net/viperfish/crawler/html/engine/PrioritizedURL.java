package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A URL that has a priority in respect to fetching. This class is safe for multi-threading.
 */
public final class PrioritizedURL {

	private AtomicInteger priority;
	private URL toFetch;

	/**
	 * creates a priority url with the url and the priority.
	 *
	 * @param toFetch the url to fetch.
	 * @param priority the priority of the url.
	 */
	public PrioritizedURL(URL toFetch, int priority) {
		if (toFetch == null) {
			throw new IllegalArgumentException("URL cannot be null");
		}

		this.toFetch = toFetch;
		this.priority = new AtomicInteger(priority);
	}

	/**
	 * get the url to fetch.
	 *
	 * @return the url to fetch.
	 */
	public URL getToFetch() {
		return toFetch;
	}

	/**
	 * get the priority of the url.
	 *
	 * @return the priority number for this url.
	 */
	public int getPriority() {
		return priority.get();
	}

	/**
	 * increment the priority number by one.
	 */
	public void increasePriority() {
		priority.incrementAndGet();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PrioritizedURL that = (PrioritizedURL) o;
		return Objects.equals(toFetch, that.toFetch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(toFetch);
	}
}