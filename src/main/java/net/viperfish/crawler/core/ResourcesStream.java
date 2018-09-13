package net.viperfish.crawler.core;

import java.io.IOException;

public interface ResourcesStream<T> extends AutoCloseable {

	T next() throws IOException;

	boolean isEndReached();

	boolean isClosed();

}
