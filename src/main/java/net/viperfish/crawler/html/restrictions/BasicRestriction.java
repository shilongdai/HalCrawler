package net.viperfish.crawler.html.restrictions;

import net.viperfish.crawler.html.Restriction;

/**
 * A base implementation of the Restriction based on simple boolean values.
 */
public class BasicRestriction implements Restriction {

	private boolean canCrawl;
	private boolean canIndex;

	/**
	 * creates a new BaseRestriction with specified boolean restrictions.
	 *
	 * @param canCrawl if the crawler can crawl the url.
	 * @param canIndex if the crawler can process the content.
	 */
	public BasicRestriction(boolean canCrawl, boolean canIndex) {
		this.canCrawl = canCrawl;
		this.canIndex = canIndex;
	}

	@Override
	public boolean canIndex() {
		return canIndex;
	}

	@Override
	public boolean canFetch() {
		return canCrawl;
	}
}
