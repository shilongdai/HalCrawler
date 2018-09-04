package net.viperfish.crawler.dao;

import net.viperfish.crawler.core.Header;

public final class HeaderDatabase extends ORMLiteDatabase<Long, Header> {

	public HeaderDatabase() {
		super(Header.class);
	}

}
