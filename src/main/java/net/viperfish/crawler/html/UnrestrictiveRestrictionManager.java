package net.viperfish.crawler.html;

import java.net.URL;

public class UnrestrictiveRestrictionManager implements RestrictionManager {

	@Override
	public Restriction getRestriction(URL url) {
		return new UnrestrictedRestriction();
	}
}
