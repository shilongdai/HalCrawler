package net.viperfish.crawler.html.dao;

import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.TextContent;

public class TextContentDatabase extends ORMLiteDatabase<Long, TextContent> {

	public TextContentDatabase() {
		super(TextContent.class);
	}

}
