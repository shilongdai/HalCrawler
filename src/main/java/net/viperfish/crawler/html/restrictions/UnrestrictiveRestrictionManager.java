package net.viperfish.crawler.html.restrictions;

import java.net.URL;
import net.viperfish.crawler.html.Restriction;
import net.viperfish.crawler.html.RestrictionManager;

/**
 * A restriction manager that does not restrict against any URLs.
 */
public class UnrestrictiveRestrictionManager implements RestrictionManager {

	@Override
	public Restriction getRestriction(URL url) {
		return new UnrestrictedRestriction();
	}
}
