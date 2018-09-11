package net.viperfish.crawler.core;

import java.util.Objects;

/**
 * A generic class for storing two values of any type.
 * @param <T1> the type of the first value.
 * @param <T2> the type of the second value.
 */
public class Pair<T1, T2> {

	private T1 first;
	private T2 second;

	/**
	 * creates a new {@link Pair} with null values.
	 */
	public Pair() {

	}

	/**
	 * creates a new {@link Pair} with the specified values.
	 * @param first
	 * @param second
	 */
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	// Getters and Setters.

	public T1 getFirst() {
		return first;
	}

	public void setFirst(T1 first) {
		this.first = first;
	}

	public T2 getSecond() {
		return second;
	}

	public void setSecond(T2 second) {
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(getFirst(), pair.getFirst()) &&
			Objects.equals(getSecond(), pair.getSecond());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirst(), getSecond());
	}
}
