package net.viperfish.crawler.exceptions;

public class ParsingException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -2868261596658935849L;

	public ParsingException() {
	}

	public ParsingException(String message) {
		super(message);
	}

	public ParsingException(Throwable cause) {
		super(cause);
	}

	public ParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParsingException(String message, Throwable cause, boolean enableSuppression,
		boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
