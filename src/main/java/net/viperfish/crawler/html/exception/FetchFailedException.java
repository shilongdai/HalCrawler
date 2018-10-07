package net.viperfish.crawler.html.exception;

import java.io.IOException;
import java.net.URL;

public class FetchFailedException extends IOException {

	private URL failedURL;

	public FetchFailedException(Throwable e, URL failed) {
		super(failed.toString(), e);
		failedURL = failed;
	}

	public FetchFailedException(URL failedURL) {
		super(failedURL.toString());
		this.failedURL = failedURL;
	}

	public FetchFailedException(Throwable e) {
		super(e);
	}

	public URL getFailedURL() {
		return failedURL;
	}

	public void setFailedURL(URL failedURL) {
		this.failedURL = failedURL;
	}
}
