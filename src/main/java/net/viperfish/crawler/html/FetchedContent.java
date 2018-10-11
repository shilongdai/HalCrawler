package net.viperfish.crawler.html;

import java.net.URL;
import java.util.Objects;

/**
 * A class containing downloaded contents by the {@link HttpFetcher}. It is a simple POJO class
 * designed to just hold data.
 */
public class FetchedContent {

	private URL url;
	private int status;
	private String html;

	/**
	 * creates a new FetchedContent with specified data
	 *
	 * @param url the URL of the site.
	 * @param status the HTTP return status.
	 * @param html the downloaded HTML.
	 */
	public FetchedContent(URL url, int status, String html) {
		this.url = url;
		this.status = status;
		this.html = html;
	}

	/**
	 * gets the URL of the site.
	 *
	 * @return the URL of the site.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * sets the URL of the site.
	 *
	 * @param url the URL of the site.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * gets the status of the HTTP Response.
	 *
	 * @return the status of the HTTP response.
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * gets the result HTML from the fetch.
	 *
	 * @return the result HTML
	 */
	public String getHtml() {
		return html;
	}

	/**
	 * sets the HTML.
	 *
	 * @param html the HTML
	 */
	public void setHtml(String html) {
		this.html = html;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FetchedContent that = (FetchedContent) o;
		return getStatus() == that.getStatus() &&
			Objects.equals(getUrl(), that.getUrl()) &&
			Objects.equals(getHtml(), that.getHtml());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUrl(), getStatus(), getHtml());
	}
}
