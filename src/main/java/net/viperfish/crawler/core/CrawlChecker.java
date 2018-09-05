package net.viperfish.crawler.core;

import java.net.URL;

public interface CrawlChecker {
	public boolean shouldCrawl(URL url, Site site);

	public boolean shouldCrawl(URL url);

	public boolean lock(URL url, Site s);
}
