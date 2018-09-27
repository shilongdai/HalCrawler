package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.TagData;
import net.viperfish.crawler.html.TagDataType;
import net.viperfish.crawler.html.TagProcessor;
import org.jsoup.nodes.Element;

public class TextSectionProcessor implements TagProcessor {

	private static final Set<String> BLOCK_TAGS;

	static {
		BLOCK_TAGS = new HashSet<>();
		BLOCK_TAGS.add("p");
		BLOCK_TAGS.add("blockquote");
		BLOCK_TAGS.add("pre");
		BLOCK_TAGS.add("code");
		BLOCK_TAGS.add("table");
	}

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site) {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		List<TagData> tags = new LinkedList<>();

		TagData td = new TagData(TagDataType.HTML_TEXT_CONTENT);
		if (!tag.tagName().equalsIgnoreCase("div")) {
			td.set("text", tag.text());
		} else {
			td.set("text", tag.ownText());
		}
		tags.add(td);
		result.put(TagDataType.HTML_TEXT_CONTENT, tags);
		return result;
	}


	@Override
	public boolean match(Element e) {
		if (e.ownText() == null || e.ownText().trim().isEmpty()) {
			return false;
		}
		if (e.tagName().equalsIgnoreCase("div") && e.ownText().length() > 75) {
			return true;
		}
		return BLOCK_TAGS.contains(e.tagName());
	}
}
