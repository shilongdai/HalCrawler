package net.viperfish.crawler.html;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * A POJO class for representing a header in the html text. It is associated with the "Header" table
 * in the database. This class is not originally designed for thread safety.
 */
@DatabaseTable(tableName = "Header")
public class Header {

	@DatabaseField
	private long siteID;
	@DatabaseField(generatedId = true)
	private long headerID;
	@DatabaseField
	private int size;
	@DatabaseField
	private String content;

	/**
	 * creates a new Header with a siteID of default IDs of -1 and a size of 0.
	 */
	public Header() {
		siteID = -1;
		size = 0;
		headerID = -1;
	}

	// Getters and Setters.

	public long getSiteID() {
		return siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getHeaderID() {
		return headerID;
	}

	public void setHeaderID(long headerID) {
		this.headerID = headerID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (int) (headerID ^ (headerID >>> 32));
		result = prime * result + (int) (siteID ^ (siteID >>> 32));
		result = prime * result + size;
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
		Header other = (Header) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (headerID != other.headerID) {
			return false;
		}
		if (siteID != other.siteID) {
			return false;
		}
		return size == other.size;
	}

}
