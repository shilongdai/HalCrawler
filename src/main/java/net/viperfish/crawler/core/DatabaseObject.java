package net.viperfish.crawler.core;

import java.io.IOException;
import java.util.Collection;

public interface DatabaseObject<ID, T> {

	public void save(T s) throws IOException;

	public void save(Collection<T> collection) throws IOException;

	public void removeSite(ID id) throws IOException;

	public T find(ID id) throws IOException;

	public Collection<T> find(Collection<ID> ids) throws IOException;

	public void close() throws IOException;
}
