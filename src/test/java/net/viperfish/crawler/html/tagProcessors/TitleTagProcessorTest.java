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

public class TitleTagProcessorTest {

	@Test
	public void testTitleTag() throws ParsingException {
		Element e = new Element("title");
		e.text("Test Title");

		TitileTagProcessor titleTagProcessor = new TitileTagProcessor();
		Map<TagDataType, List<TagData>> result = titleTagProcessor.processTag(e, new Site());

		Assert.assertEquals(true, result.containsKey(TagDataType.HTML_TITLE));
		Assert.assertEquals(1, result.get(TagDataType.HTML_TITLE).size());
		Assert.assertEquals("Test Title",
			result.get(TagDataType.HTML_TITLE).get(0).get("title", String.class));
	}

}
