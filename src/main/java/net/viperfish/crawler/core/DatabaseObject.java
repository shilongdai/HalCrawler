package net.viperfish.crawler.core;

import java.io.IOException;
import java.util.Collection;

public interface DatabaseObject<ID, T> {

	void save(T s) throws IOException;

	void save(Collection<T> collection) throws IOException;

	void removeSite(ID id) throws IOException;

	T find(ID id) throws IOException;

	Collection<T> find(Collection<ID> ids) throws IOException;

	void close() throws IOException;
}
