package net.viperfish.crawler.html;

import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The base representation of a site that has been crawled. It contains basic information about the
 * site such as the title, url, checksum, content, and anchors. In addition, it supports properties
 * to be associated with a site that can be customized by {@link TagProcessor}s.
 */
public class CrawledData implements Serializable {

	private static final long serialVersionUID = 1;

	private String title;
	private URL url;
	private String checksum;
	private String content;
	private List<Anchor> anchors;
	private ConcurrentMap<String, Object> properties;

	/**
	 * creates a new CrawledData object with no contents.
	 */
	public CrawledData() {
		properties = new ConcurrentHashMap<>();
	}

	/**
	 * gets the title of the site.
	 *
	 * @return the title of the site.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * sets the title of the site.
	 *
	 * @param title the title of the site
	 */
	public void setTitle(String title) {
		if (title == null) {
			throw new NullPointerException("Title is null");
		}
		this.title = title;
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
		if (url == null) {
			throw new NullPointerException("URL cannot be null");
		}

		this.url = url;
	}

	/**
	 * gets the content of the site.
	 *
	 * @return the content of the site (probably HTML).
	 */
	public String getContent() {
		return content;
	}

	/**
	 * sets the content of the site.
	 *
	 * @param content the content of the site.
	 */
	public void setContent(String content) {
		if (content == null) {
			throw new NullPointerException("Content cannot be null");
		}

		this.content = content;
	}

	/**
	 * gets the checksum of the site.
	 *
	 * @return the checksum of the site.
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * sets the checksum of the site.
	 *
	 * @param checksum the checksum of the site.
	 */
	public void setChecksum(String checksum) {
		if (checksum == null) {
			throw new NullPointerException("Checksum cannot be null");
		}

		this.checksum = checksum;
	}

	/**
	 * gets the list of anchors on the site.
	 *
	 * @return the list of anchors on the site.
	 */
	public List<Anchor> getAnchors() {
		return anchors;
	}

	/**
	 * sets the list of anchors on the site.
	 *
	 * @param anchors the anchors on the site.
	 */
	public void setAnchors(List<Anchor> anchors) {
		this.anchors = new LinkedList<>(anchors);
	}

	/**
	 * gets the additional properties associated with this text.
	 *
	 * @return the properties. Changes to the map will be reflected.
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

	/**
	 * sets a property.
	 *
	 * @param name the name of the property
	 * @param o the data associated with the property.
	 */
	public void setProperty(String name, Object o) {
		properties.put(name, o);
	}

	/**
	 * gets a property by its type and name.
	 *
	 * @param name the name of the property.
	 * @param type the type or super type of the property
	 * @param <T> the return type
	 * @return the property data or null if no matching property found.
	 */
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
