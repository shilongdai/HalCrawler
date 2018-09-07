package net.viperfish.crawler.dao;

import net.viperfish.crawler.core.EmphasizedTextContent;

public class EmphasizedTextDatabase extends ORMLiteDatabase<Long, EmphasizedTextContent> {

	public EmphasizedTextDatabase() {
		super(EmphasizedTextContent.class);
	}

}
