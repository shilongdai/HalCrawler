package net.viperfish.crawler.html.tagProcessors;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import net.viperfish.crawler.exceptions.ParsingException;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.TagData;
import net.viperfish.crawler.html.TagDataType;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class ALinkTagProcessorTest {

	@Test
	public void testUrl() throws MalformedURLException, ParsingException {
		Element e = new Element("a");
		e.attr("href", "https://www.google.com");
		e.text("Google");
		Site testSite = new Site();
		testSite.setTitle("test");
		testSite.setUrl(new URL("http://www.example.com/index.html"));
		testSite.setSiteID(1);

		ALinkTagProcessor processor = new ALinkTagProcessor();
		Map<TagDataType, List<TagData>> result = processor.processTag(e, testSite);
		List<TagData> linkData = result.get(TagDataType.HTML_LINK);
		TagData td = linkData.get(0);

		Assert.assertEquals(true, result.containsKey(TagDataType.HTML_LINK));
		Assert.assertEquals(1, linkData.size());
		Assert.assertEquals(new URL("https://www.google.com"), td.get("url", URL.class));
		Assert.assertEquals("Google", td.get("anchor", String.class));
	}

}
