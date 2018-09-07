package net.viperfish.crawler.base;

import java.util.List;
import java.util.Map;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;

public interface TagProcessor {

	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site)
		throws ParsingException;
}
