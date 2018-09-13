package net.viperfish.crawler.html;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import net.viperfish.crawler.core.Anchor;
import net.viperfish.crawler.html.dao.URlDataPersister;

/**
 * A POJO class for representing and storing a crawled html webpage. It is associated with the table
 * "Site" and contains {@link Header}s, {@link TextContent}s, and {@link EmphasizedTextContent}s. It
 * is not designed for thread safety.
 */
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
	private List<Anchor> anchors;

	/**
	 * creates a new Site with no contents.
	 */
	public Site() {
		siteID = -1;
		headers = new LinkedList<>();
		texts = new LinkedList<>();
		emphasizedTexts = new LinkedList<>();
		anchors = new LinkedList<>();
	}

	// Getters and Setters.

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

	public List<Anchor> getAnchors() {
		return anchors;
	}

	public void setAnchors(List<Anchor> anchors) {
		this.anchors = anchors;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Site site = (Site) o;
		return Objects.equals(getTitle(), site.getTitle()) &&
			Objects.equals(getUrl(), site.getUrl()) &&
			Objects.equals(getChecksum(), site.getChecksum());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getTitle(), getUrl(), getChecksum());
	}
}
