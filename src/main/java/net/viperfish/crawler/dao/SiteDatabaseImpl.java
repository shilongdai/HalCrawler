package net.viperfish.crawler.dao;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import net.viperfish.crawler.core.Header;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.SiteDatabase;
import net.viperfish.crawler.core.TextContent;

public class SiteDatabaseImpl extends ORMLiteDatabase<Long, Site> implements SiteDatabase {

	private ORMLiteDatabase<Long, Header> headerDB;
	private ORMLiteDatabase<Long, TextContent> textContentDB;

	public SiteDatabaseImpl(ORMLiteDatabase<Long, Header> hDb, ORMLiteDatabase<Long, TextContent> tDB) {
		super(Site.class);
		this.headerDB = hDb;
		this.textContentDB = tDB;
	}

	@Override
	public void save(Site s) throws IOException {
		super.save(s);
		setSiteIDs(s.getHeaders(), s);
		setTextContentID(s.getTexts(), s);
		try {
			headerDB.save(s.getHeaders());
			textContentDB.save(s.getTexts());
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public Site find(Long id) throws IOException {
		Site result = super.find(id);
		if (result != null) {
			result.setHeaders(headerDB.findBy("siteID", result.getSiteID()));
			result.setTexts(textContentDB.findBy("siteID", result.getSiteID()));
		}
		return result;
	}

	private void setSiteIDs(Iterable<Header> headers, Site s) {
		for (Header h : headers) {
			h.setSiteID(s.getSiteID());
		}
	}

	private void setTextContentID(Iterable<TextContent> textContents, Site s) {
		for (TextContent t : textContents) {
			t.setSiteID(s.getSiteID());
		}
	}

	@Override
	public Site find(URL url) throws IOException {
		try {
			List<Site> result = this.dao().queryForEq("url", url);
			if (!result.isEmpty()) {
				return result.get(0);
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

}
