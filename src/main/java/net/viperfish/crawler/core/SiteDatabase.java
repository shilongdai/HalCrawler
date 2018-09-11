package net.viperfish.crawler.core;

import java.io.IOException;
import java.net.URL;

/**
 * A {@link DatabaseObject} for storing Sites.
 */
public interface SiteDatabase extends DatabaseObject<Long, Site> {

	/**
	 * finds a specific site by looking up its unique URL.
	 * @param url the url to lookup.
	 * @return the site associated with the URL, or <code>null</code> if no site found.
	 * @throws IOException if any error occurred on query.
	 */
	Site find(URL url) throws IOException;
}
