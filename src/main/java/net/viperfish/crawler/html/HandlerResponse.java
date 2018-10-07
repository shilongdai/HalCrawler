package net.viperfish.crawler.html;

public enum HandlerResponse {
	GO_AHEAD(0), NO_INDEX(1), HALT(3), DEFERRED(2);

	private int weight;

	HandlerResponse(int weight) {
		this.weight = weight;
	}

	boolean overrides(HandlerResponse resp) {
		return this.weight > resp.weight;
	}
}
