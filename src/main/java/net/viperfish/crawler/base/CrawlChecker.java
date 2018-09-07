package net.viperfish.crawler.base;

import java.net.URL;
import net.viperfish.crawler.core.Site;

public interface CrawlChecker {

	public boolean shouldCrawl(URL url, Site site);

	public boolean shouldCrawl(URL url);

	public boolean lock(URL url, Site s);
}
