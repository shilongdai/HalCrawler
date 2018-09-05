package net.viperfish.crawler.crawlChecker;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.viperfish.crawler.core.CrawlChecker;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.SiteDatabase;

public class URLCrawlChecker implements CrawlChecker {

	private SiteDatabase siteDB;
	private Cache<URL, String> cache;

	public URLCrawlChecker(SiteDatabase db) {
		this.siteDB = db;
		cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
	}

	@Override
	public boolean shouldCrawl(URL url, Site site) {
		if (cache.asMap().containsKey(url)) {
			return false;
		}
		try {
			Site s = siteDB.find(url);
			if (s == null) {
				return true;
			}
			return cache.asMap().putIfAbsent(url, "") == null;
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
		return cache.asMap().putIfAbsent(url, "") == null;
	}

	@Override
	public boolean shouldCrawl(URL url) {
		return shouldCrawl(url, null);
	}

}
