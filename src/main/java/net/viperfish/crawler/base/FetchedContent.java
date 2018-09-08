package net.viperfish.crawler.base;

import java.net.URL;
import java.util.Objects;

public class FetchedContent {

	private URL url;
	private int status;
	private String rowHTML;

	public FetchedContent(URL url, int status, String rowHTML) {
		this.url = url;
		this.status = status;
		this.rowHTML = rowHTML;
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

	public String getRowHTML() {
		return rowHTML;
	}

	public void setRowHTML(String rowHTML) {
		this.rowHTML = rowHTML;
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
			Objects.equals(getRowHTML(), that.getRowHTML());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUrl(), getStatus(), getRowHTML());
	}
}
