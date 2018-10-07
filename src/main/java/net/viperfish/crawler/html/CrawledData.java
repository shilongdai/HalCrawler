package net.viperfish.crawler.html;

import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrawledData implements Serializable {

	private static final long serialVersionUID = 1;

	private String title;
	private URL url;
	private String checksum;
	private String content;
	private List<Anchor> anchors;
	private ConcurrentMap<String, Object> properties;

	public CrawledData() {
		properties = new ConcurrentHashMap<>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
		this.title = url.toString();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public List<Anchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(List<Anchor> anchors) {
		this.anchors = anchors;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperty(String name, Object o) {
		properties.put(name, o);
	}

	public <T> T getProperty(String name, Class<T> type) {
		Object result = properties.get(name);
		if (type.isInstance(result)) {
			return type.cast(result);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CrawledData that = (CrawledData) o;
		return Objects.equals(title, that.title) &&
			Objects.equals(url, that.url) &&
			Objects.equals(checksum, that.checksum) &&
			Objects.equals(content, that.content) &&
			Objects.equals(anchors, that.anchors) &&
			Objects.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(title, url, checksum, content, anchors, properties);
	}
}
