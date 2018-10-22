package net.viperfish.crawler.html;

import net.viperfish.crawler.html.exception.ParsingException;
import org.jsoup.nodes.Document;

/**
 * A processor that processes HTML elements. This interface is used to customize the output of the
 * {@link HttpWebCrawler}.
 */
public interface TagProcessor {

	/**
	 * processes the html document and modifies the {@link CrawledData}
	 *
	 * @param document the document to parse.
	 * @throws ParsingException if failed to parse.
	 */
	void processTag(Document document, CrawledData crawledData)
		throws ParsingException;


}
