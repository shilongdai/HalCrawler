package net.viperfish.crawler.core;

import java.io.IOException;
import java.net.URL;

public interface SiteDatabase extends DatabaseObject<Long, Site> {
	public Site find(URL url) throws IOException;
}
