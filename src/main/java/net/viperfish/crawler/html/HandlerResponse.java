package net.viperfish.crawler.html;

/**
 * The control signals returned by the {@link HttpCrawlerHandler}s. These signals controls the flow
 * of the site processing. If different signals are returned by different handlers, the signal with
 * the greatest weight overrides the other signals.
 */
public enum HandlerResponse {
	/**
	 * Go ahead and continue processing.
	 */
	GO_AHEAD(0),
	/**
	 * Go ahead but do not send the result to the {@link net.viperfish.crawler.core.Datasink}.
	 */
	NO_INDEX(1),
	/**
	 * Stop immediately and move on to the next site.
	 */
	HALT(3),
	/**
	 * Stop immediately and postpone the processing of this site until later.
	 */
	DEFERRED(2);

	private int weight;

	/**
	 * initializes the weight of this signal type.
	 *
	 * @param weight the weight of the signal.
	 */
	HandlerResponse(int weight) {
		this.weight = weight;
	}

	/**
	 * checks if this signal overrides the other signal based on weight.
	 *
	 * @param resp the other signal.
	 * @return true if overrides, false otherwise.
	 */
	boolean overrides(HandlerResponse resp) {
		return this.weight > resp.weight;
	}
}
