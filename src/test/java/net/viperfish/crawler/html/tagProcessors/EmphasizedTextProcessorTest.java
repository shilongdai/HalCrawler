package net.viperfish.crawler.html.tagProcessors;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;

public class EmphasizedTextProcessorTest {

	@Test
	public void testEmphasizedContentProcessor() throws ParsingException {
		Element e = new Element("b");
		e.text("This is a bolded text");

		EmphasizedTagProcessor processor = new EmphasizedTagProcessor();

		Map<TagDataType, List<TagData>> result = processor.processTag(e, new Site());
		List<TagData> eTexts = result.get(TagDataType.HTML_EMPHASIZED_TEXT);

		Assert.assertEquals(1, eTexts.size());
	}
}
