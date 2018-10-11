package net.viperfish.crawler.html.restrictions;

/**
 * A restriction that indicates everything is allowed.
 */
public class UnrestrictedRestriction extends BasicRestriction {

	public UnrestrictedRestriction() {
		super(true, true);
	}
}
