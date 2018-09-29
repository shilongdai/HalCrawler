package net.viperfish.crawler.html;

import java.net.URL;

/**
 * A manager of {@link Restriction}s.
 */
public interface RestrictionManager {

	/**
	 * gets the {@link Restriction} for the specified URL.
	 *
	 * @param url the url to get restriction for.
	 * @return the restriction for the specified URL.
	 */
	Restriction getRestriction(URL url);

}
