package net.viperfish.crawler.crawlChecker;

import java.net.URL;
import net.viperfish.crawler.html.CrawlChecker;
import net.viperfish.crawler.html.Site;

public final class NullCrawlChecker implements CrawlChecker {

	public NullCrawlChecker() {
	}

	@Override
	public boolean shouldCrawl(URL url, Site s) {
		return true;
	}

	@Override
	public boolean lock(URL url, Site s) {
		return true;
	}

	@Override
	public boolean shouldCrawl(URL url) {
		return true;
	}

}
