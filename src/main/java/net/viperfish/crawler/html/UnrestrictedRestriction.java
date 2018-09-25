package net.viperfish.crawler.html;

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
