package net.viperfish.crawler;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.HttpFetcher;
import net.viperfish.crawler.html.HttpWebCrawler;
import net.viperfish.crawler.html.InMemSiteDatabase;
import net.viperfish.crawler.html.crawlHandler.Limit2HostHandler;
import net.viperfish.crawler.html.engine.ApplicationConcurrentHttpFetcher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.junit.Assert;
import org.junit.Test;


public class TestCrawler {


	@Test
	public void testBasicCrawler()
		throws Exception {
		URL url2Test = new URL("https://example.com");
		HttpFetcher fetcher = new ApplicationConcurrentHttpFetcher(1);
		InMemSiteDatabase siteDB = new InMemSiteDatabase();
		siteDB.init();
		HttpWebCrawler crawler = new HttpWebCrawler(1, siteDB,
			fetcher);
		crawler.registerCrawlerHandler(new Limit2HostHandler(new URL("https://example.com")));
		crawler.submit(new URL("https://example.com/"));
		Date begin = new Date();
		crawler.startProcessing();
		crawler.waitUntiDone();
		Date end = new Date();
		System.out.println("Submission Time:" + (end.getTime() - begin.getTime()));
		crawler.shutdown();
		fetcher.close();

		CrawledData crawled = siteDB.get(new URL("https://example.com/"));

		String rawHTML = new String(IOUtil.read(url2Test.openStream()), StandardCharsets.UTF_8);

		Digest md5 = new MD5Digest();
		md5.update(rawHTML.getBytes(StandardCharsets.UTF_8), 0,
			rawHTML.getBytes(StandardCharsets.UTF_8).length);
		byte[] hashOut = new byte[16];
		md5.doFinal(hashOut, 0);

		Assert.assertEquals(new URL("https://example.com/"), crawled.getUrl());
		Assert.assertEquals(rawHTML, crawled.getContent());
		Assert.assertEquals(Base64.getEncoder().encodeToString(hashOut), crawled.getChecksum());
	}
}
