package net.viperfish.crawler.core;

import java.net.URL;

public interface HttpWebCrawler {

	void submit(URL url);

	void limitToHost(boolean limit);

	boolean isLimitedToHost();

	void shutdown();

	boolean isIdle();
}
