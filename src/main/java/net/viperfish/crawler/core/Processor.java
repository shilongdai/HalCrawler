package net.viperfish.crawler.core;

/**
 * A class that do some kind of processing. The specific kind depends on the implementations.
 */
public interface Processor {

	/**
	 * starts to do the processing operation.
	 */
	void startProcessing();

	/**
	 * checks if the processing is complete.
	 *
	 * @return <i>true</i> if complete, <i>false</i> otherwise.
	 */
	boolean isDone();

	/**
	 * blocks and wait until the processing is complete.
	 *
	 * @throws InterruptedException if an interruption occurred.
	 */
	void waitUntiDone() throws InterruptedException;

	/**
	 * resets the processor to its initial state before processing occurred.
	 */
	void reset();

	/**
	 * terminates the processing and cleanup all resources.
	 */
	void shutdown();
}
