package net.viperfish.crawler.dao;

import net.viperfish.crawler.core.Anchor;

public class AnchorDatabase extends ORMLiteDatabase<Long, Anchor> {

	public AnchorDatabase() {
		super(Anchor.class);
	}

}
