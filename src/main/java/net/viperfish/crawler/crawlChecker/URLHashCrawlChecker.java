package net.viperfish.crawler.crawlChecker;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.viperfish.crawler.html.CrawlChecker;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.SiteDatabase;

public class URLHashCrawlChecker implements CrawlChecker {

	private SiteDatabase siteDB;
	private ConcurrentMap<URL, Boolean> urlCache;
	private ConcurrentMap<String, Boolean> hashCache;

	public URLHashCrawlChecker(SiteDatabase db) {
		this.siteDB = db;
		hashCache = new ConcurrentHashMap<>();
		urlCache = new ConcurrentHashMap<>();
	}

	@Override
	public boolean shouldCrawl(URL url, Site site) {
		if (urlCache.containsKey(url) || hashCache.containsKey(site.getChecksum())) {
			return false;
		}
		try {
			Site byHash = siteDB.find(site.getChecksum());
			if (byHash != null) {
				urlCache.putIfAbsent(url, true);
				hashCache.putIfAbsent(site.getChecksum(), true);
				return false;
			}
			return true;
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
		boolean lockURL = urlCache.putIfAbsent(url, true) == null;
		boolean lockHash = hashCache.putIfAbsent(s.getChecksum(), true) == null;
		return lockHash && lockURL;
	}

	@Override
	public boolean shouldCrawl(URL url) {
		if (urlCache.containsKey(url)) {
			return false;
		}
		try {
			Site byURL = siteDB.find(url);
			if (byURL == null) {
				return true;
			}
			urlCache.putIfAbsent(url, true);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
