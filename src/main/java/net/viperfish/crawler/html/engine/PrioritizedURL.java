package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class PrioritizedURL {

	private AtomicInteger priority;
	private URL toFetch;

	public PrioritizedURL(URL toFetch, int priority) {
		if (toFetch == null) {
			throw new IllegalArgumentException("URL cannot be null");
		}

		this.toFetch = toFetch;
		this.priority = new AtomicInteger(priority);
	}

	public URL getToFetch() {
		return toFetch;
	}

	public int getPriority() {
		return priority.get();
	}

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