package net.viperfish.crawler.core;

import java.net.URL;
import java.util.Map;

public interface HttpWebCrawler {
	public Map<Long, URL> crawl(URL url);

	public void limitToHost(boolean limit);

	public boolean isLimitedToHost();

	public void shutdown();

	public void setCrawlChecker(CrawlChecker checker);

	public CrawlChecker getCrawlChecker();
}
