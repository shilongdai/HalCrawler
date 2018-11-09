package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import net.viperfish.crawler.html.FetchedContent;

/**
 * A PriorityBooster that determines the boost factor using {@link URL}.
 */
public abstract class URLPriorityBooster extends BasePriorityBooster {

	@Override
	protected int getBoostFactor(FetchedContent content) {
		return getURLBoostFactor(content.getUrl().getToFetch());
	}

	/**
	 * gets the boost factor based on url.
	 *
	 * @param url the url of the site.
	 * @return the boost factor.
	 */
	protected abstract int getURLBoostFactor(URL url);

}
