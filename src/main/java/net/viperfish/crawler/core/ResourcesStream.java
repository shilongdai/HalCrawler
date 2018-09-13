package net.viperfish.crawler.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface ResourcesStream<T> extends AutoCloseable {

	T next() throws IOException;

	T next(long timeout, TimeUnit unit) throws IOException;

	boolean isEndReached();

	boolean isClosed();

}
