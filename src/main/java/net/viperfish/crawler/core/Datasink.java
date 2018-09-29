package net.viperfish.crawler.core;

import java.io.IOException;

/**
 * An output sink that data can be written to. This class is used by the {@link DataProcessor} to
 * write data output. All implementation of this class must be thread safe.
 *
 * @param <T> the type of output data
 */
public interface Datasink<T> extends AutoCloseable {

	/**
	 * initializes the object.
	 *
	 * @throws IOException if failed to initialize.
	 */
	void init() throws IOException;

	/**
	 * writes data to the sink.
	 *
	 * @param data the data to write
	 * @throws IOException if failed to write data
	 */
	void write(T data) throws IOException;

	/**
	 * checks if this sink is closed.
	 *
	 * @return true if closed, false otherwise
	 */
	boolean isClosed();
}
