package net.viperfish.crawler.html.restrictions;

import net.viperfish.crawler.html.Restriction;

public class UnrestrictedRestriction implements Restriction {

	@Override
	public boolean canIndex() {
		return true;
	}

	@Override
	public boolean canFetch() {
		return true;
	}
}
