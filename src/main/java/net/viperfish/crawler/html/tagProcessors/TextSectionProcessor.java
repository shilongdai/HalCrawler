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

public class TextSectionProcessor implements TagProcessor {

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site) {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		List<TagData> tags = new LinkedList<>();

		TagData td = new TagData(TagDataType.HTML_TEXT_CONTENT);
		if (tag.isBlock() && !tag.tagName().equalsIgnoreCase("div")) {
			td.set("text", tag.text());
		} else {
			td.set("text", tag.ownText());
		}
		tags.add(td);
		result.put(TagDataType.HTML_TEXT_CONTENT, tags);
		return result;
	}

	@Override
	public boolean shouldProcess(Element e) {
		if (e.text() == null || e.text().trim().isEmpty()) {
			return false;
		}
		if (e.parent() == null) {
			return true;
		}
		if (e.parent().tagName().equalsIgnoreCase("body")) {
			return true;
		}
		if (e.parent().tagName().equalsIgnoreCase("div")) {
			return true;
		}
		for (Element i : e.parents()) {
			if (i.isBlock()) {
				return false;
			}
		}
		return true;

	}
}
