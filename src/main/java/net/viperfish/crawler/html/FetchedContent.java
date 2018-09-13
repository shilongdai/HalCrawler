package net.viperfish.crawler.html;

import java.net.URL;
import java.util.Objects;

public class FetchedContent {

	private URL url;
	private int status;
	private String rawHTML;

	public FetchedContent(URL url, int status, String rawHTML) {
		this.url = url;
		this.status = status;
		this.rawHTML = rawHTML;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getRawHTML() {
		return rawHTML;
	}

	public void setRawHTML(String rawHTML) {
		this.rawHTML = rawHTML;
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
			Objects.equals(getRawHTML(), that.getRawHTML());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUrl(), getStatus(), getRawHTML());
	}
}
