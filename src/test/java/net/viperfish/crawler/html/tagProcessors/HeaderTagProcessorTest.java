package net.viperfish.crawler.html.tagProcessors;

import java.util.List;
import java.util.Map;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.TagData;
import net.viperfish.crawler.html.TagDataType;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class HeaderTagProcessorTest {

	@Test
	public void testHeaderTagProcessor() {
		HeaderTagProcessor processor = new HeaderTagProcessor();

		Element e = new Element("h2");
		e.text("This is a header");

		Site site = new Site();

		Map<TagDataType, List<TagData>> result = processor.processTag(e, site);

		Assert.assertEquals(1, result.size());

		List<TagData> tags = result.get(TagDataType.HTML_HEADER_CONTENT);
		Assert.assertEquals(1, tags.size());

		TagData td = tags.get(0);

		Assert.assertEquals("This is a header", td.get("headerText", String.class));
		Assert.assertEquals("2", td.get("size", String.class));
	}
}
