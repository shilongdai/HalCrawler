package net.viperfish.crawler.html.crawlChecker;

import java.net.URL;
import net.viperfish.crawler.html.HandlerResponse;

public class Limit2HostHandler extends YesCrawlChecker {

	private URL thisURL;

	public Limit2HostHandler(URL thisURL) {
		this.thisURL = thisURL;
	}

	@Override
	public HandlerResponse handlePreFetch(URL url) {
		if (url.getHost().equals(thisURL.getHost())) {
			return HandlerResponse.GO_AHEAD;
		}
		return HandlerResponse.HALT;
	}
}
