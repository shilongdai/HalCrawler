package net.viperfish.crawler.core;

import java.io.IOException;
import java.util.Collection;

/**
 * A database that can be used to store objects. All implementing classes must ensure that the
 * underlying mechanics guarantee thread safety.
 *
 * @param <ID> the type of the lookup id
 * @param <T> the type of the object
 */
public interface DatabaseObject<ID, T> extends Datasink<T> {

	/**
	 * saves the object. The object s would be saved. An auto-generated id should be assigned to the
	 * object.
	 *
	 * @param s the object to save.
	 * @throws IOException if error occurred on saving.
	 */
	void save(T s) throws IOException;

	/**
	 * saves a group of objects. All of the objects would be saved with a unique id assigned to
	 * each.
	 *
	 * @param collection the collection to save.
	 * @throws IOException if error occurred on save.
	 */
	void save(Collection<T> collection) throws IOException;

	/**
	 * remove an individual object by the id. If the id is not associated with and specific object,
	 * nothing would be done.
	 *
	 * @param id the id assigned to the object.
	 * @throws IOException if error occurred on deletion.
	 */
	void remove(ID id) throws IOException;

	/**
	 * query for an object with the assigned id. If no object is associated with the id,
	 * <code>null</code> would be returned.
	 *
	 * @param id the id assigned to the object.
	 * @return the object, or <code>null</code> if nothing found.
	 * @throws IOException if any error occurred on query.
	 */
	T find(ID id) throws IOException;

	/**
	 * find a group of objects from a group of ids. If a specific id has no association with any
	 * object, it will be skipped in the result.
	 *
	 * @param ids the group of ids.
	 * @return a collection of objects associated with the collection of ids.
	 * @throws IOException if error occurred on query.
	 */
	Collection<T> find(Collection<ID> ids) throws IOException;

	/**
	 * closes all resources used by this {@link DatabaseObject}. It should ensure that no resources
	 * leak would occur.
	 *
	 * @throws IOException if failed to free up resources.
	 */
	void close() throws IOException;
}
