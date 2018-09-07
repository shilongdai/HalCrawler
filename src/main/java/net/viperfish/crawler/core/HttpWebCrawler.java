package net.viperfish.crawler.core;

import java.net.URL;
import java.util.concurrent.BlockingQueue;

public interface HttpWebCrawler {
	public void submit(URL url);

	public void limitToHost(boolean limit);

	public boolean isLimitedToHost();

	public void shutdown();

	public void setCrawlChecker(CrawlChecker checker);

	public CrawlChecker getCrawlChecker();

	public BlockingQueue<Long> getResults();

	public boolean isIdle();
}
