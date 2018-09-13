package net.viperfish.crawler.html;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.net.URL;
import net.viperfish.crawler.html.dao.URlDataPersister;

/**
 * An anchor on a page, usually an a tag. This class is a POJO java object and is associated with
 * the Database Table "Anchor". It is not designed for thread safety.
 */
@DatabaseTable(tableName = "Anchor")
public class Anchor {

	@DatabaseField(generatedId = true)
	private long anchorID;
	@DatabaseField
	private long siteID;
	@DatabaseField
	private String anchorText;
	@DatabaseField(persisterClass = URlDataPersister.class)
	private URL targetURL;

	/**
	 * creates a new anchor with an id of -1 and the associated site as -1.
	 */
	public Anchor() {
		anchorID = -1;
		siteID = -1;
	}

	// Getters and Setters

	public long getAnchorID() {
		return anchorID;
	}

	public void setAnchorID(long anchorID) {
		this.anchorID = anchorID;
	}

	public long getSiteID() {
		return siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (anchorID ^ (anchorID >>> 32));
		result = prime * result + ((anchorText == null) ? 0 : anchorText.hashCode());
		result = prime * result + (int) (siteID ^ (siteID >>> 32));
		result = prime * result + ((targetURL == null) ? 0 : targetURL.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Anchor other = (Anchor) obj;
		if (anchorID != other.anchorID) {
			return false;
		}
		if (anchorText == null) {
			if (other.anchorText != null) {
				return false;
			}
		} else if (!anchorText.equals(other.anchorText)) {
			return false;
		}
		if (siteID != other.siteID) {
			return false;
		}
		if (targetURL == null) {
			return other.targetURL == null;
		} else {
			return targetURL.equals(other.targetURL);
		}
	}

}
