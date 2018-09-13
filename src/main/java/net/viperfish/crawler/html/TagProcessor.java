package net.viperfish.crawler.html;

import java.util.List;
import java.util.Map;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;

public interface TagProcessor {

	Map<TagDataType, List<TagData>> processTag(Element tag, Site site)
		throws ParsingException;

	boolean shouldProcess(Element e);
}
