package net.viperfish.crawler.html.exception;

import java.io.IOException;
import java.net.URL;

/**
 * An exception where an attempt to download the resource via HTTP failed. This exception contains
 * the attempted URL.
 */
public class FetchFailedException extends IOException {

	private URL failedURL;

	/**
	 * creates a new exception with the standard Java exception and the URL.
	 *
	 * @param e the standard exception that occurred.
	 * @param failed the failed URL.
	 */
	public FetchFailedException(Throwable e, URL failed) {
		super(failed.toString(), e);
		failedURL = failed;
	}

	/**
	 * creates a new exception with the failed URL.
	 *
	 * @param failedURL the URL with error
	 */
	public FetchFailedException(URL failedURL) {
		super(failedURL.toString());
		this.failedURL = failedURL;
	}

	/**
	 * creates a new exception with the original standard exception.
	 *
	 * @param e the original exception.
	 */
	public FetchFailedException(Throwable e) {
		super(e);
	}

	/**
	 * gets the url that failed.
	 *
	 * @return the failed url or null if unspecified.
	 */
	public URL getFailedURL() {
		return failedURL;
	}

	/**
	 * sets the failed url.
	 *
	 * @param failedURL the failed url.
	 */
	public void setFailedURL(URL failedURL) {
		this.failedURL = failedURL;
	}
}
