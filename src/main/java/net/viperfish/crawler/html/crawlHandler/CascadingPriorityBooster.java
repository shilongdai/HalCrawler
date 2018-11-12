package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.viperfish.crawler.html.Anchor;
import net.viperfish.crawler.html.CrawledData;
import net.viperfish.crawler.html.FetchedContent;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.engine.PrioritizedURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A priority booster that attempts to boost the homepage of a site by a specified amount. It also
 * boosts any pages linked from the parent page by the priority of the parent page with a dampener.
 * The formula for the boosting is priority = this priority + dampener * (parent priority).C
 */
public class CascadingPriorityBooster extends YesCrawlChecker {

	private int boostFactor;
	private double dampener;
	private Map<URL, URL> parentTracker;
	private Map<URL, Integer> priorityTracker;
	private Logger logger;

	/**
	 * create a booster with specified boost factor.
	 *
	 * @param boostAmount the boost factor for main pages.
	 * @param dampener the dampener that reduces the boost for secondary pages.
	 */
	public CascadingPriorityBooster(int boostAmount, double dampener) {
		this.boostFactor = boostAmount;
		this.dampener = dampener;
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.parentTracker = new ConcurrentHashMap<>();
		this.priorityTracker = new ConcurrentHashMap<>();
	}

	@Override
	public HandlerResponse handlePostParse(CrawledData site) {
		for (Anchor a : site.getAnchors()) {
			logger.debug("Adding {} as child of {}", a.getTargetURL(), site.getUrl());
			parentTracker.putIfAbsent(a.getTargetURL(), site.getUrl());
		}
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreParse(FetchedContent content) {
		if (isMainPage(content.getUrl().getSource())) {
			logger.debug("{} is main page, boosting by {}", content.getUrl().getSource(),
				boostFactor);
			priorityTracker.putIfAbsent(content.getUrl().getSource(), boostFactor);
			content.getUrl().increasePriority(boostFactor);
		}
		priorityTracker.putIfAbsent(content.getUrl().getSource(), content.getUrl().getPriority());
		return HandlerResponse.GO_AHEAD;
	}

	@Override
	public HandlerResponse handlePreFetch(PrioritizedURL url) {
		URL parent = parentTracker.get(url.getSource());
		logger.debug("Cascading priority from {} to {}", parent, url.getSource());
		Integer parentPriority = priorityTracker.get(parent);
		int boostedAmount = (int) (parentPriority * dampener);
		logger.debug("Boosting {} by {}", url.getSource(), boostedAmount);
		url.increasePriority(boostedAmount);
		return HandlerResponse.GO_AHEAD;
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
