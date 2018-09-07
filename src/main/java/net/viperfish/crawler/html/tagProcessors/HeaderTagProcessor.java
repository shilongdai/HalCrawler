package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.viperfish.crawler.base.TagProcessor;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;

public final class HeaderTagProcessor implements TagProcessor {

	public HeaderTagProcessor() {
	}

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site)
		throws ParsingException {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		result.put(TagDataType.HTML_HEADER_CONTENT, new LinkedList<>());

		TagData td = new TagData(TagDataType.HTML_HEADER_CONTENT);
		td.set("headerText", tag.text());
		if (tag.tagName().length() == 2) {
			td.set("size", tag.tagName().substring(1, 2));
		} else {
			td.set("size", "1");
		}

		result.get(TagDataType.HTML_HEADER_CONTENT).add(td);
		return result;
	}

}
