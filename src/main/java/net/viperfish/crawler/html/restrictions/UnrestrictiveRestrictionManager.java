package net.viperfish.crawler.html.restrictions;

import java.net.URL;
import net.viperfish.crawler.html.Restriction;
import net.viperfish.crawler.html.RestrictionManager;

public class UnrestrictiveRestrictionManager implements RestrictionManager {

	@Override
	public Restriction getRestriction(URL url) {
		return new UnrestrictedRestriction();
	}
}
