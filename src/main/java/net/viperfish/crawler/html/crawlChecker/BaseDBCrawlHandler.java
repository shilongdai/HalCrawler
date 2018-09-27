package net.viperfish.crawler.html.crawlChecker;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.SiteDatabase;

public class BaseDBCrawlHandler extends BaseCrawlHandler {

	private SiteDatabase siteDB;
	private ConcurrentMap<URL, Boolean> urlCache;
	private ConcurrentMap<String, Boolean> hashCache;

	public BaseDBCrawlHandler(SiteDatabase db) {
		this.siteDB = db;
		hashCache = new ConcurrentHashMap<>();
		urlCache = new ConcurrentHashMap<>();
	}

	@Override
	protected boolean isParsed(Site site) {
		if (urlCache.containsKey(site.getUrl()) || hashCache.containsKey(site.getChecksum())) {
			return true;
		}
		try {
			Site byHash = siteDB.find(site.getChecksum());
			if (byHash != null) {
				urlCache.putIfAbsent(site.getUrl(), true);
				hashCache.putIfAbsent(site.getChecksum(), true);
				return true;
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}

	@Override
	protected boolean lock(Site s) {
		boolean lockURL = urlCache.putIfAbsent(s.getUrl(), true) == null;
		boolean lockHash = hashCache.putIfAbsent(s.getChecksum(), true) == null;
		return lockHash && lockURL;
	}

	@Override
	protected boolean isFetched(URL url) {
		if (urlCache.containsKey(url)) {
			return true;
		}
		try {
			Site byURL = siteDB.find(url);
			if (byURL == null) {
				return false;
			}
			urlCache.putIfAbsent(url, true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return true;
		}
	}

}
