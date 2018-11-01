package net.viperfish.crawler.core;

public interface Processor {

	void startProcessing();

	boolean isDone();

	void waitUntiDone() throws InterruptedException;

	void reset();

	void shutdown();
}
