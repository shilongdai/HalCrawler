package net.viperfish.crawler.html.restrictions;

import net.viperfish.crawler.html.Restriction;

public class BasicRestriction implements Restriction {

	private boolean canCrawl;
	private boolean canIndex;

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
