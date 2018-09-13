package net.viperfish.crawler.html.dao;

import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.Anchor;

public class AnchorDatabase extends ORMLiteDatabase<Long, Anchor> {

	public AnchorDatabase() {
		super(Anchor.class);
	}

}
