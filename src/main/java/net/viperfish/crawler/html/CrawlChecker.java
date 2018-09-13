package net.viperfish.crawler.html;

import java.net.URL;

public interface CrawlChecker {

	boolean shouldCrawl(URL url, Site site);

	boolean shouldCrawl(URL url);

	boolean lock(URL url, Site s);
}
