package net.viperfish.crawler.html;

import java.net.URL;
import java.util.Objects;

public class FetchedContent {

	private URL url;
	private int status;
	private String html;

	public FetchedContent(URL url, int status, String html) {
		this.url = url;
		this.status = status;
		this.html = html;
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

	public String getHtml() {
		return html;
	}

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
