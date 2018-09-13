package net.viperfish.crawler.crawlChecker;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.viperfish.crawler.html.CrawlChecker;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.SiteDatabase;

public class URLCrawlChecker implements CrawlChecker {

	private SiteDatabase siteDB;
	private ConcurrentMap<URL, String> cache;

	public URLCrawlChecker(SiteDatabase db) {
		this.siteDB = db;
		cache = new ConcurrentHashMap<>();
	}

	@Override
	public boolean shouldCrawl(URL url, Site site) {
		if (cache.containsKey(url)) {
			return false;
		}
		try {
			Site s = siteDB.find(url);
			if (s == null) {
				return true;
			}
			cache.putIfAbsent(url, "");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean lock(URL url, Site s) {
		if (!shouldCrawl(url)) {
			return false;
		}
		return cache.putIfAbsent(url, "") == null;
	}

	@Override
	public boolean shouldCrawl(URL url) {
		return shouldCrawl(url, null);
	}

}
