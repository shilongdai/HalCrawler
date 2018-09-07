package net.viperfish.crawler.core;

import java.net.URL;

public interface HttpWebCrawler {
	public void submit(URL url);

	public void limitToHost(boolean limit);

	public boolean isLimitedToHost();

	public void shutdown();

	public boolean isIdle();
}
