package net.viperfish.crawler.html.dao;

import net.viperfish.crawler.core.Anchor;
import net.viperfish.crawler.core.ORMLiteDatabase;

public class AnchorDatabase extends ORMLiteDatabase<Long, Anchor> {

	public AnchorDatabase() {
		super(Anchor.class);
	}

}
