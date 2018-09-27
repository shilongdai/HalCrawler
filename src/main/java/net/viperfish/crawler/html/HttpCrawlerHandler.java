package net.viperfish.crawler.html;

import java.net.URL;

public interface HttpCrawlerHandler {

	HandlerResponse handlePostParse(Site site);

	HandlerResponse handlePreFetch(URL url);

	HandlerResponse handlePostProcess(Site site);
}
