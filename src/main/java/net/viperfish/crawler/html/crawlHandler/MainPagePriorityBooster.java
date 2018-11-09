package net.viperfish.crawler.html.crawlHandler;

import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link URLPriorityBooster} that attempts to boost the homepage of a site by a specified
 * factor.
 */
public class MainPagePriorityBooster extends URLPriorityBooster {

	private int boostFactor;
	private Logger logger;

	/**
	 * create a booster with specified boost factor.
	 *
	 * @param boostFactor the boost factor.
	 */
	public MainPagePriorityBooster(int boostFactor) {
		this.boostFactor = boostFactor;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	protected int getURLBoostFactor(URL url) {
		String ending = url.getHost();
		logger.debug("Checking {} against {}", url.toExternalForm(), ending);
		if (url.toExternalForm().endsWith(ending) || url.toExternalForm().endsWith(ending + "/")) {
			logger.debug("{} is main page, boosting by {}", url, Integer.toString(boostFactor));
			return boostFactor;
		}
		return 1;
	}
}
