package net.viperfish.crawler.html.tagProcessors;

import java.util.List;
import java.util.Map;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class PTagProcessorTest {

	@Test
	public void testProcessor() throws ParsingException {
		TextOwnTagsProcessor processor = new TextOwnTagsProcessor();
		Element element = new Element("p");
		element.text("Paragraph Here");
		Site site = new Site();

		Map<TagDataType, List<TagData>> result = processor.processTag(element, site);

		Assert.assertEquals(1, result.size());

		List<TagData> tags = result.get(TagDataType.HTML_TEXT_CONTENT);
		Assert.assertEquals(1, tags.size());

		TagData td = tags.get(0);

		Assert.assertEquals("Paragraph Here", td.get("text", String.class));
	}

}
