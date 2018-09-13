package net.viperfish.crawler.core;

import java.io.IOException;

public interface Datasink<T> extends AutoCloseable {

	void write(T data) throws IOException;

	boolean isClosed();
}
