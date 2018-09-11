package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.viperfish.crawler.base.TagProcessor;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Assert;
import org.junit.Test;

public class TextSectionProcessorTest {

	@Test
	public void testTextSection() throws ParsingException {
		TextSectionProcessor processor = new TextSectionProcessor();
		Document doc = Jsoup.parse(
			"<div>This is a section<p>This is a paragraph<q>This is a quote</q></p><blockquote>To be or not to be</blockquote></div>");

		Map<TagDataType, List<TagData>> result = recursiveInterpretTags(
			doc.getElementsByTag("div").first(), new Site(),
			processor);
		List<TagData> textSections = result.get(TagDataType.HTML_TEXT_CONTENT);
		System.out.println(textSections);

		Assert.assertEquals(3, textSections.size());
	}

	private Map<TagDataType, List<TagData>> recursiveInterpretTags(Element e, Site s,
		TagProcessor processor)
		throws ParsingException {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		if (e == null || e.tagName() == null) {
			return result;
		}
		if (processor.shouldProcess(e)) {
			result = processor.processTag(e, s);
		}
		for (Element child : e.children()) {
			Map<TagDataType, List<TagData>> childResult = recursiveInterpretTags(child, s,
				processor);
			for (Entry<TagDataType, List<TagData>> entry : childResult.entrySet()) {
				if (!result.containsKey(entry.getKey())) {
					result.put(entry.getKey(), entry.getValue());
				} else {
					for (TagData td : entry.getValue()) {
						result.get(entry.getKey()).add(td);
					}
				}
			}
		}
		return result;
	}

}
