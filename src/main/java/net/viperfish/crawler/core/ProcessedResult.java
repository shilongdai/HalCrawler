package net.viperfish.crawler.core;

import java.util.Objects;

/**
 * The result of the processing operation. This class is used by the {@link DataProcessor} as a data
 * structure that stored the result from the processing. It contains the actual result as well as
 * meta-information regarding this result.
 */
public class ProcessedResult<T> {

	private T result;
	private boolean shouldOutput;

	/**
	 * creates a new ProcessedResult with specified result and index information.
	 *
	 * @param result the result of the processing.
	 * @param index whether this result should be indexed.
	 */
	public ProcessedResult(T result, boolean index) {
		this.result = result;
		this.shouldOutput = index;
	}

	/**
	 * sets whether the processor is allowed to send the result to the {@link Datasink}.
	 *
	 * @param allowed true if the processor should send the result to the {@link Datasink} false
	 * otherwise.
	 */
	public void allowOutput(boolean allowed) {
		shouldOutput = allowed;
	}

	/**
	 * gets whether the processor is allowed to send the result to the {@link Datasink}.
	 *
	 * @return true if the processor should send the result to the {@link Datasink} false otherwise.
	 */
	public boolean shouldOutput() {
		return shouldOutput;
	}

	/**
	 * gets the result of the processing.
	 *
	 * @return the result of processing.
	 */
	public T getResult() {
		return result;
	}

	/**
	 * sets the result of the processing
	 *
	 * @param result the result of the processing.
	 */
	public void setResult(T result) {
		this.result = result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProcessedResult<?> that = (ProcessedResult<?>) o;
		return shouldOutput == that.shouldOutput &&
			Objects.equals(result, that.result);
	}

	@Override
	public int hashCode() {
		return Objects.hash(result, shouldOutput);
	}
}
