package net.viperfish.crawler.core;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.viperfish.crawler.dao.URlDataPersister;

@DatabaseTable(tableName = "Site")
public final class Site {

	@DatabaseField(generatedId = true)
	private long siteID;
	@DatabaseField
	private String title;
	@DatabaseField(persisterClass = URlDataPersister.class)
	private URL url;
	@DatabaseField
	private String checksum;
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] compressedHtml;

	private List<Header> headers;
	private List<TextContent> texts;
	private List<EmphasizedTextContent> emphasizedTexts;

	public Site() {
		siteID = -1;
		headers = new LinkedList<>();
		texts = new LinkedList<>();
		emphasizedTexts = new LinkedList<>();
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

	public byte[] getCompressedHtml() {
		return compressedHtml.clone();
	}

	public void setCompressedHtml(byte[] compressedHtml) {
		this.compressedHtml = compressedHtml;
	}

	public long getSiteID() {
		return siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public List<TextContent> getTexts() {
		return texts;
	}

	public void setTexts(List<TextContent> texts) {
		this.texts = texts;
	}

	public List<EmphasizedTextContent> getEmphasizedTexts() {
		return emphasizedTexts;
	}

	public void setEmphasizedTexts(List<EmphasizedTextContent> emphasizedTexts) {
		this.emphasizedTexts = emphasizedTexts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
		result = prime * result + Arrays.hashCode(compressedHtml);
		result = prime * result + ((emphasizedTexts == null) ? 0 : emphasizedTexts.hashCode());
		result = prime * result + ((headers == null) ? 0 : headers.hashCode());
		result = prime * result + (int) (siteID ^ (siteID >>> 32));
		result = prime * result + ((texts == null) ? 0 : texts.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Site other = (Site) obj;
		if (checksum == null) {
			if (other.checksum != null) {
				return false;
			}
		} else if (!checksum.equals(other.checksum)) {
			return false;
		}
		if (!Arrays.equals(compressedHtml, other.compressedHtml)) {
			return false;
		}
		if (emphasizedTexts == null) {
			if (other.emphasizedTexts != null) {
				return false;
			}
		} else if (!emphasizedTexts.equals(other.emphasizedTexts)) {
			return false;
		}
		if (headers == null) {
			if (other.headers != null) {
				return false;
			}
		} else if (!headers.equals(other.headers)) {
			return false;
		}
		if (siteID != other.siteID) {
			return false;
		}
		if (texts == null) {
			if (other.texts != null) {
				return false;
			}
		} else if (!texts.equals(other.texts)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		if (url == null) {
			return other.url == null;
		} else {
			return url.equals(other.url);
		}
	}

}
