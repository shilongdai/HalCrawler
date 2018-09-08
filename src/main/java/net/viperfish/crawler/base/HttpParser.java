package net.viperfish.crawler.base;

import java.util.List;
import java.util.Map;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;

public interface HttpParser {

	void submit(FetchedContent content);

	Map<TagDataType, List<TagData>> nextParsed() throws ParsingException;
}
