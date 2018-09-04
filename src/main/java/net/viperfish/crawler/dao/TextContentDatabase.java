package net.viperfish.crawler.dao;

import net.viperfish.crawler.core.TextContent;

public class TextContentDatabase extends ORMLiteDatabase<Long, TextContent> {

	public TextContentDatabase() {
		super(TextContent.class);
	}

}
