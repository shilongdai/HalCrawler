package net.viperfish.crawler.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A data input stream that provides data. This class is used by the {@link ConcurrentDataProcessor}
 * to get new data. All implementation of this interface must be thread safe.
 *
 * @param <T> the type of data input
 */
public interface ResourcesStream<T> extends AutoCloseable {

	/**
	 * gets the next data in the stream. This method should either return null if no data exists, or
	 * block until data is available.
	 *
	 * @return the next data or null if no data.
	 * @throws IOException if failed to get the next data in the stream.
	 */
	T next() throws IOException;

	/**
	 * gets the next data in the stream with timeout. This method should block until new data is
	 * available or if timeout occurs, which ever one happens first. If the timeout occurs, it
	 * should return null.
	 *
	 * @param timeout the time out in respect to the unit
	 * @param unit the time unit of the time out
	 * @return the next data or null if timed out.
	 * @throws IOException if failed to retrieve the next data.
	 */
	T next(long timeout, TimeUnit unit) throws IOException;

	/**
	 * checks if the cursor reached the end of the stream. If this method returns true, no new data
	 * should ever be available in the stream in the current context.
	 *
	 * @return true if end of stream reached, false otherwise.
	 */
	boolean isEndReached();

	/**
	 * checks if the stream is closed.
	 *
	 * @return true if stream closed, false otherwise.
	 */
	boolean isClosed();

}
