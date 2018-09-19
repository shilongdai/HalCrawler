package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.TagData;
import net.viperfish.crawler.html.TagDataType;
import net.viperfish.crawler.html.TagProcessor;
import org.jsoup.nodes.Element;

public final class TitileTagProcessor implements TagProcessor {

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site) {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		List<TagData> resultData = new LinkedList<>();

		TagData parsedTag = new TagData(TagDataType.HTML_TITLE);
		parsedTag.set("title", tag.text());

		resultData.add(parsedTag);

		result.put(TagDataType.HTML_TITLE, resultData);

		return result;
	}

	@Override
	public boolean match(Element e) {
		boolean notEmpty = e.text() != null && !e.text().isEmpty();
		return notEmpty && e.tagName().equalsIgnoreCase("title");
	}
}
