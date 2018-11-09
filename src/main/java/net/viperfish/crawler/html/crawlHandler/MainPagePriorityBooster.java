package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.viperfish.crawler.html.Anchor;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link URLPriorityBooster} that attempts to boost the homepage of a site by a specified factor.
 * It also boosts any pages linked from the parent page by the priority of the parent page with a
 * dampener.
 */
public class MainPagePriorityBooster extends BasePriorityBooster {

	private int boostFactor;
	private double dampener;
	private Map<URL, URL> parentTracker;
	private Map<URL, Integer> priorityTracker;
	private Logger logger;

	/**
	 * create a booster with specified boost factor.
	 *
	 * @param boostFactor the boost factor for main pages.
	 * @param dampener the dampener that reduces the boost for secondary pages.
	 */
	public MainPagePriorityBooster(int boostFactor, double dampener) {
		this.boostFactor = boostFactor;
		this.dampener = dampener;
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.parentTracker = new ConcurrentHashMap<>();
		this.priorityTracker = new ConcurrentHashMap<>();
	}

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
		for (Anchor a : site.getAnchors()) {
			parentTracker.putIfAbsent(site.getUrl(), a.getTargetURL());
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	protected int getBoostFactor(FetchedContent content) {
		if (isMainPage(content.getUrl().getToFetch())) {
			logger.debug("{} is main page, boosting by {}", content.getUrl().getToFetch(),
				Integer.toString(boostFactor));
			priorityTracker.putIfAbsent(content.getUrl().getToFetch(), boostFactor);
			return boostFactor;
		}
		if (parentTracker.containsKey(content.getUrl().getToFetch())) {
			URL parentURL = parentTracker.get(content.getUrl().getToFetch());
			logger.debug("Cascading priority from {} to {}", parentURL,
				content.getUrl().getToFetch());
			int boostMultiplier = (int) Math
				.round(((double) priorityTracker.get(parentURL) * dampener));
			priorityTracker.putIfAbsent(content.getUrl().getToFetch(), boostMultiplier);
			logger.debug("boosting {} by {}", content.getUrl().getToFetch(), boostMultiplier);
			return boostMultiplier;
		}
		return 1;
	}

	/**
	 * attempts to determine if a page is the home page of a site.
	 *
	 * @param url the url of the page.
	 * @return true if homepage, false otherwise.
	 */
	private boolean isMainPage(URL url) {
		String ending = url.getHost();
		logger.debug("Checking {} against {}", url.toExternalForm(), ending);
		return url.toExternalForm().endsWith(ending) || url.toExternalForm().endsWith(ending + "/");
	}
}
