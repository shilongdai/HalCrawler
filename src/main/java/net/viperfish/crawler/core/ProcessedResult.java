package net.viperfish.crawler.core;

public class ProcessedResult<T> extends Pair<T, Boolean> {

	public ProcessedResult(T first, Boolean second) {
		super(first, second);
	}

	public void allowIndex(boolean allowed) {
		this.setSecond(allowed);
	}

	public boolean shouldIndex() {
		return this.getSecond();
	}

	public T getResult() {
		return this.getFirst();
	}

	public void setResult(T result) {
		this.setFirst(result);
	}
}
