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

	public String getAnchorText() {
		return anchorText;
	}

	public void setAnchorText(String anchorText) {
		this.anchorText = anchorText;
	}

	public URL getTargetURL() {
		return targetURL;
	}

	public void setTargetURL(URL targetURL) {
		this.targetURL = targetURL;
	}

	public int getSize() {
		return size;
	}

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
