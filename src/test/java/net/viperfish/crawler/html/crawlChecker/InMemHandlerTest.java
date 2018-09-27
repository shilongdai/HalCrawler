package net.viperfish.crawler.html.crawlChecker;

import java.net.MalformedURLException;
import java.net.URL;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.Site;
import org.junit.Assert;
import org.junit.Test;

public class InMemHandlerTest {


	@Test
	public void testURLChecker() throws MalformedURLException {
		Site existingSite = new Site();
		existingSite.setChecksum("12345");
		existingSite.setCompressedHtml(new byte[10]);
		existingSite.setTitle("Existing Site");
		existingSite.setUrl(new URL("https://www.example.com"));
		BaseInMemCrawlHandler checker = new BaseInMemCrawlHandler();
		checker.lock(existingSite);

		Assert.assertEquals(HandlerResponse.HALT,
			checker.handlePreFetch(new URL("https://www.example.com")));
		Assert.assertEquals(HandlerResponse.GO_AHEAD,
			checker.handlePreFetch(new URL("https://google.com")));

		Site exampleSite = new Site();
		exampleSite.setUrl(new URL("https://exe.com"));
		exampleSite.setChecksum("7890");

		Site identicalSite = new Site();
		identicalSite.setUrl(new URL("https://exe.com/index?parameter=this"));
		identicalSite.setChecksum("7890");

		Assert.assertEquals(HandlerResponse.GO_AHEAD, checker.handlePostParse(exampleSite));
		Assert.assertEquals(HandlerResponse.HALT, checker.handlePostParse(identicalSite));
		Assert
			.assertEquals(HandlerResponse.HALT, checker.handlePreFetch(identicalSite.getUrl()));
	}
}
