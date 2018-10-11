package net.viperfish.crawler.html;

import java.net.URL;
import java.util.Objects;

/**
 * An anchor on a page, usually an a tag. This class is a POJO java object with information about
 * the anchor. It is not designed for thread safety.
 */
public class Anchor {

	private String anchorText;
	private URL targetURL;
	private int size;

	/**
	 * creates a new anchor with an id of -1 and the associated site as -1.
	 */
	public Anchor() {
		size = 0;
	}

	/**
	 * gets the text of this anchor.
	 *
	 * @return the text of the anchor.
	 */
	public String getAnchorText() {
		return anchorText;
	}

	/**
	 * sets the text field of this POJO.
	 *
	 * @param anchorText the text of the anchor.
	 */
	public void setAnchorText(String anchorText) {
		this.anchorText = anchorText;
	}

	/**
	 * gets the URL of the anchor.
	 *
	 * @return the URL.
	 */
	public URL getTargetURL() {
		return targetURL;
	}

	/**
	 * sets the URL of the anchor.
	 *
	 * @param targetURL the url of the anchor.
	 */
	public void setTargetURL(URL targetURL) {
		this.targetURL = targetURL;
	}

	/**
	 * gets the size of the anchor.
	 *
	 * @return the size of the anchor.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * sets the size of the anchor.
	 *
	 * @param size the size of the anchor.
	 */
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Anchor anchor = (Anchor) o;
		return size == anchor.size &&
			Objects.equals(anchorText, anchor.anchorText) &&
			Objects.equals(targetURL, anchor.targetURL);
	}

	@Override
	public int hashCode() {
		return Objects.hash(anchorText, targetURL, size);
	}
}
