package net.viperfish.crawler.html;

import java.io.IOException;
import java.net.URL;
import net.viperfish.crawler.core.DatabaseObject;
import net.viperfish.crawler.core.Datasink;

/**
 * A {@link DatabaseObject} for storing Sites.
 */
public interface SiteDatabase extends DatabaseObject<Long, Site>, Datasink<Site> {

	/**
	 * finds a specific site by looking up its unique URL.
	 *
	 * @param url the url to lookup.
	 * @return the site associated with the URL, or <code>null</code> if no site found.
	 * @throws IOException if any error occurred on query.
	 */
	Site find(URL url) throws IOException;

	/**
	 * finds a specific site by looking up its unique checksum
	 *
	 * @param checksum the checksum to lookup
	 * @return the site found or null if not found
	 * @throws IOException if a database error occurred.
	 */
	Site find(String checksum) throws IOException;
}
