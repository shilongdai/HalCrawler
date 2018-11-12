package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

/**
 * A URL that has a priority in respect to fetching. This class is safe for multi-threading.
 */
public final class PrioritizedURL {

	private AtomicInteger priority;
	private URL source;

	/**
	 * creates a priority url with the url and the priority.
	 *
	 * @param source the url to fetch.
	 * @param priority the priority of the url.
	 */
	public PrioritizedURL(URL source, int priority) {
		if (source == null) {
			throw new IllegalArgumentException("URL cannot be null");
		}

		this.source = source;
		this.priority = new AtomicInteger(priority);
	}

	/**
	 * get the url to fetch.
	 *
	 * @return the url to fetch.
	 */
	public URL getSource() {
		return source;
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

	/**
	 * increment the priority number by the specified amount.
	 *
	 * @param amount the amount to increment;
	 */
	public void increasePriority(int amount) {
		priority.accumulateAndGet(amount, new IntBinaryOperator() {
			@Override
			public int applyAsInt(int left, int right) {
				return left + right;
			}
		});
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
		return Objects.equals(source, that.source);
	}

	@Override
	public int hashCode() {
		return Objects.hash(source);
	}
}