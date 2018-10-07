package net.viperfish.crawler.html.crawlChecker;

import java.net.MalformedURLException;
import java.net.URL;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.HandlerResponse;
import org.junit.Assert;
import org.junit.Test;

public class InMemHandlerTest {


	@Test
	public void testURLChecker() throws MalformedURLException {
		CrawledData existingSite = new CrawledData();
		existingSite.setChecksum("12345");
		existingSite.setContent("");
		existingSite.setTitle("Existing Site");
		existingSite.setUrl(new URL("https://www.example.com"));
		BaseInMemCrawlHandler checker = new BaseInMemCrawlHandler();
		checker.lock(existingSite);

		Assert.assertEquals(HandlerResponse.HALT,
			checker.handlePreFetch(new URL("https://www.example.com")));
		Assert.assertEquals(HandlerResponse.GO_AHEAD,
			checker.handlePreFetch(new URL("https://google.com")));

		CrawledData exampleSite = new CrawledData();
		exampleSite.setUrl(new URL("https://exe.com"));
		exampleSite.setChecksum("7890");

		CrawledData identicalSite = new CrawledData();
		identicalSite.setUrl(new URL("https://exe.com/index?parameter=this"));
		identicalSite.setChecksum("7890");

		Assert.assertEquals(HandlerResponse.GO_AHEAD, checker.handlePostParse(exampleSite));
		Assert.assertEquals(HandlerResponse.HALT, checker.handlePostParse(identicalSite));
		Assert
			.assertEquals(HandlerResponse.HALT, checker.handlePreFetch(identicalSite.getUrl()));
	}
}
