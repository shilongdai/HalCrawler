package net.viperfish.crawler.html;

public enum HandlerResponse {
	GO_AHEAD(0), HALT(2), DEFERRED(1);

	private int weight;

	HandlerResponse(int weight) {
		this.weight = weight;
	}

	boolean overrides(HandlerResponse resp) {
		return this.weight > resp.weight;
	}
}
