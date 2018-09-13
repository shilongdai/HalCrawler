package net.viperfish.crawler.html.dao;

import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.EmphasizedTextContent;

public class EmphasizedTextDatabase extends ORMLiteDatabase<Long, EmphasizedTextContent> {

	public EmphasizedTextDatabase() {
		super(EmphasizedTextContent.class);
	}

}
