package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.viperfish.crawler.base.TagProcessor;
import net.viperfish.crawler.core.EmphasizedType;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;

public class EmphasizedTagProcessor implements TagProcessor {

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site)
		throws ParsingException {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		List<TagData> tags = new LinkedList<>();
		TagData tgData = new TagData();
		switch (tag.tagName()) {
			case "b": {
				tgData.set("method", EmphasizedType.BOLD);
				break;
			}
			case "em": {
				tgData.set("method", EmphasizedType.ITALIC);
				break;
			}
			case "strong": {
				tgData.set("method", EmphasizedType.BOLD);
				break;
			}
			default: {
				tgData.set("method", EmphasizedType.BOLD);
			}
		}
		tgData.set("content", tag.text());
		tags.add(tgData);

		result.put(TagDataType.HTML_EMPHASIZED_TEXT, tags);
		return result;
	}

}
