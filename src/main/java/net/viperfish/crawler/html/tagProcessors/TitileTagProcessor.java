package net.viperfish.crawler.html.tagProcessors;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

import net.viperfish.crawler.base.TagProcessor;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;

public final class TitileTagProcessor implements TagProcessor {

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site) throws ParsingException {
		Map<TagDataType, List<TagData>> result = new HashMap<>();
		List<TagData> resultData = new LinkedList<>();

		TagData parsedTag = new TagData(TagDataType.HTML_TITLE);
		parsedTag.set("title", tag.text());

		resultData.add(parsedTag);

		result.put(TagDataType.HTML_TITLE, resultData);

		return result;
	}

}
