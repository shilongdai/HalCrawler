package net.viperfish.crawler.core;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "TextContent")
public class TextContent {

	@DatabaseField
	private long siteID;
	@DatabaseField(generatedId = true)
	private long textID;
	@DatabaseField
	private String content;

	public TextContent() {
		siteID = -1;
	}

	public long getSiteID() {
		return siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getTextID() {
		return textID;
	}

	public void setTextID(long textID) {
		this.textID = textID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (int) (siteID ^ (siteID >>> 32));
		result = prime * result + (int) (textID ^ (textID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextContent other = (TextContent) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (siteID != other.siteID)
			return false;
		if (textID != other.textID)
			return false;
		return true;
	}

}
