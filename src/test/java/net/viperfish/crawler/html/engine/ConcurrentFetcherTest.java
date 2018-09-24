package net.viperfish.crawler.html.engine;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.html.FetchedContent;
import org.junit.Assert;
import org.junit.Test;

public class ConcurrentFetcherTest {

	@Test
	public void testFetcher() throws IOException {
		URL testSite = new URL("https://www.viperfish.net/test/testCrawl.html");
		String pageHTML = new String(IOUtil.read(testSite.openStream()), StandardCharsets.UTF_8);

		ConcurrentHttpFetcher fetcher = new ConcurrentHttpFetcher(2);
		fetcher.submit(testSite);
		FetchedContent content = fetcher.next();

		Assert.assertEquals(pageHTML, content.getHtml());
		Assert.assertEquals(testSite, content.getUrl());
		Assert.assertEquals(200, content.getStatus());

		fetcher.close();
	}
}
