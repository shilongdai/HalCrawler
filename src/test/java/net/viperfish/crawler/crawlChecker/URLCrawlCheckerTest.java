package net.viperfish.crawler.crawlChecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.dao.AnchorDatabase;
import net.viperfish.crawler.html.dao.EmphasizedTextDatabase;
import net.viperfish.crawler.html.dao.HeaderDatabase;
import net.viperfish.crawler.html.dao.SiteDatabaseImpl;
import net.viperfish.crawler.html.dao.TextContentDatabase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class URLCrawlCheckerTest {

	private static SiteDatabaseImpl siteDB;

	@BeforeClass
	public static void setup() throws SQLException, IOException {
		ORMLiteDatabase.connect("jdbc:h2:mem:testCheckURL", "testUser", "testPassword");
		siteDB = (SiteDatabaseImpl) new SiteDatabaseImpl(new HeaderDatabase().connect(),
			new TextContentDatabase().connect(), new EmphasizedTextDatabase().connect(),
			new AnchorDatabase().connect()).connect();
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

		Site existingSite = new Site();
		existingSite.setChecksum("12345");
		existingSite.setCompressedHtml(new byte[10]);
		existingSite.setTitle("Existing Site");
		existingSite.setUrl(new URL("https://www.example.com"));
		siteDB.save(existingSite);
	}

	@Test
	public void testURLChecker() throws MalformedURLException {
		URLCrawlChecker checker = new URLCrawlChecker(siteDB);
		Assert.assertEquals(false, checker.shouldCrawl(new URL("https://www.example.com")));

		Assert.assertEquals(true, checker.shouldCrawl(new URL("https://google.com")));
		Assert.assertEquals(true, checker.lock(new URL("https://google.com"), null));
		Assert.assertEquals(false, checker.shouldCrawl(new URL("https://google.com")));
		Assert.assertEquals(false, checker.lock(new URL("https://google.com"), null));
	}

}
