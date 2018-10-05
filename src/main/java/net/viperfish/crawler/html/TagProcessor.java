package net.viperfish.crawler.html;

import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;

/**
 * A processor of a html tag. It is automatically applied to the parsed html by {@link
 * HttpWebCrawler}.
 */
public interface TagProcessor {

	/**
	 * processes a tag element that matches this processor.
	 *
	 * @param tag the element to process
	 * @param site the current site
	 * @throws ParsingException if failed to process the element.
	 */
	void processTag(Element tag, CrawledData site)
		throws ParsingException;

	/**
	 * tests whether this processor should be applied to the specified tag.
	 *
	 * @param element the element to test against.
	 * @return true if this processor should be applied, false otherwise.
	 */
	boolean match(Element element);

}
