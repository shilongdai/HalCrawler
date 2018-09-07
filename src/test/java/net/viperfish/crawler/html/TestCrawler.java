package net.viperfish.crawler.html;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.viperfish.crawler.dao.AnchorDatabase;
import net.viperfish.crawler.dao.EmphasizedTextDatabase;
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
				new TextContentDatabase().connect(), new EmphasizedTextDatabase().connect()).connect();
		siteDB.executeSql(
				"CREATE TABLE Site(siteID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, title VARCHAR(1000) NOT NULL, url VARCHAR(1000) UNIQUE, checksum VARCHAR(128) NOT NULL, compressedHtml BLOB(14000000) NOT NULL);"
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
		siteDB.executeSql(
				"CREATE TABLE EmphasizedText(textID BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, siteID BIGINT NOT NULL, content BLOB(14000000), method VARCHAR(50) NOT NULL, CONSTRAINT Site_EmphasizedText FOREIGN KEY (siteID) REFERENCES Site(siteID) ON DELETE CASCADE);");

	}

	@Test
	public void testBasicCrawler() throws IOException {
		HtmlWebCrawler crawler = new HtmlWebCrawler(siteDB, aDB);
		crawler.submit(new URL("https://example.com/"));
		crawler.shutdown();
		Assert.assertEquals(new Long(1), crawler.getResults().poll());
		Assert.assertEquals(new URL("https://example.com/"), siteDB.find(1L).getUrl());
	}

	@AfterClass
	public static void cleanup() {
		ORMLiteDatabase.closeConn();
	}
}
