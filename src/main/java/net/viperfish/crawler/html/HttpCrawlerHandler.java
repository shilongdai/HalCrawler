package net.viperfish.crawler.html;

import java.net.URL;

public interface HttpCrawlerHandler {

	HandlerResponse handlePostParse(CrawledData site);

	HandlerResponse handlePreFetch(URL url);

	HandlerResponse handlePostProcess(CrawledData site);
}
