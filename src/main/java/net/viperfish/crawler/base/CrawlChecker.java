package net.viperfish.crawler.base;

import java.net.URL;
import net.viperfish.crawler.core.Site;

public interface CrawlChecker {

	boolean shouldCrawl(URL url, Site site);

	boolean shouldCrawl(URL url);

	boolean lock(URL url, Site s);
}
