package net.viperfish.crawler.html.dao;

import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.Header;

public final class HeaderDatabase extends ORMLiteDatabase<Long, Header> {

	public HeaderDatabase() {
		super(Header.class);
	}

}
