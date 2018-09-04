package net.viperfish.crawler.html;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.viperfish.crawler.dao.AnchorDatabase;
import net.viperfish.crawler.dao.HeaderDatabase;
import net.viperfish.crawler.dao.ORMLiteDatabase;
import net.viperfish.crawler.dao.SiteDatabaseImpl;
import net.viperfish.crawler.dao.TextContentDatabase;

public class TestCrawler {

	private static SiteDatabaseImpl siteDB;
	private static AnchorDatabase aDB;

	@BeforeClass
	public static void init() throws SQLException {
		ORMLiteDatabase.connect("jdbc:h2:mem:test", "testUser", "testPassword");
		siteDB = (SiteDatabaseImpl) new SiteDatabaseImpl(new HeaderDatabase().connect(),
				new TextContentDatabase().connect()).connect();
		aDB = (AnchorDatabase) new AnchorDatabase().connect();
		siteDB.executeSql(
				"CREATE TABLE Site(siteID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, title VARCHAR(1000) NOT NULL, url VARCHAR(3000) NOT NULL, checksum VARCHAR(128) NOT NULL, compressedHtml BLOB(14000000) NOT NULL);"
						+ "");
		siteDB.executeSql(
				"CREATE TABLE Header(headerID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, siteID BIGINT NOT NULL, size INT, content VARCHAR(1000) NOT NULL, CONSTRAINT Site_Header FOREIGN KEY (siteID) REFERENCES Site(siteID) ON DELETE CASCADE);"
						+ "");
		siteDB.executeSql(
				"CREATE TABLE Anchor(anchorID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, siteID BIGINT NOT NULL, anchorText VARCHAR(1000) NOT NULL, targetURL VARCHAR(3000) NOT NULL, CONSTRAINT Site_Anchor FOREIGN KEY (siteID) REFERENCES Site(siteID) ON DELETE CASCADE);"
						+ "");
		siteDB.executeSql(
				"CREATE TABLE TextContent(textID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, siteID BIGINT NOT NULL, content BLOB(14000000), CONSTRAINT Site_Text FOREIGN KEY (siteID) REFERENCES Site(siteID) ON DELETE CASCADE);"
						+ "");
	}

	@Test
	public void testBasicCrawler() throws IOException {
		HtmlWebCrawler crawler = new HtmlWebCrawler(siteDB, aDB);
		Map<Long, URL> result = crawler.crawl(new URL("https://example.com/"));

		URL site = result.get(1L);

		crawler.shutdown();
		Assert.assertEquals(1, result.size());
		Assert.assertEquals(new URL("https://example.com/"), site);
	}

	@AfterClass
	public static void cleanup() {
		ORMLiteDatabase.closeConn();
	}
}
