package net.viperfish.crawler.html;

import net.viperfish.crawler.html.exception.ParsingException;
import org.jsoup.nodes.Element;

/**
 * A processor that processes HTML elements. This interface is used to customize the output of the
 * {@link HttpWebCrawler}.
 */
public interface TagProcessor {

	/**
	 * processes a tag on the HTML page that matches this processor.
	 *
	 * @param tag the tag on the HTML page.
	 * @param site the currently processing site.
	 * @throws ParsingException if failed to parse.
	 */
	void processTag(Element tag, CrawledData site)
		throws ParsingException;

	/**
	 * tests whether this processor match to the specified tag.
	 *
	 * @param element the element to test against.
	 * @return true if this processor should be applied, false otherwise.
	 */
	boolean match(Element element);

}
