package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.viperfish.crawler.base.TagProcessor;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import org.jsoup.nodes.Element;

public class TextAllTagProcessor implements TagProcessor {

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site) {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		List<TagData> tags = new LinkedList<>();

		TagData td = new TagData(TagDataType.HTML_TEXT_CONTENT);
		td.set("text", tag.text());
		tags.add(td);
		result.put(TagDataType.HTML_TEXT_CONTENT, tags);
		return result;
	}

	@Override
	public boolean shouldProcess(Element e) {
		return e.text() != null && !e.text().isEmpty();
	}
}
