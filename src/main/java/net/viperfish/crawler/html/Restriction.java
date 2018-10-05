package net.viperfish.crawler.html;

/**
 * A restriction for crawling or indexing a site.
 */
public interface Restriction {

	/**
	 * tests if the crawler should save the site.
	 *
	 * @return true if the crawler can, false otherwise.
	 */
	boolean canIndex();

	/**
	 * tests if the crawler can fetch the site.
	 *
	 * @return true if the crawler can, false otherwise.
	 */
	boolean canFetch();
}
