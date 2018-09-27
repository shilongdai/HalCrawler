package net.viperfish.crawler.html.crawlChecker;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.HandlerResponse;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.dao.SiteDatabaseImpl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class URLHashHttpCrawlerHandlerTest {

	private static SiteDatabaseImpl siteDB;

	@BeforeClass
	public static void setup() throws SQLException, IOException {
		ORMLiteDatabase.connect("jdbc:h2:mem:testCheckURL", "testUser", "testPassword");
		siteDB = new SiteDatabaseImpl();
		siteDB.init();
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
		BaseDBCrawlHandler checker = new BaseDBCrawlHandler(siteDB);
		Assert.assertEquals(HandlerResponse.HALT,
			checker.handlePreFetch(new URL("https://www.example.com")));
		Assert.assertEquals(HandlerResponse.GO_AHEAD,
			checker.handlePreFetch(new URL("https://google.com")));

		Site exampleSite = new Site();
		exampleSite.setUrl(new URL("https://exe.com"));
		exampleSite.setChecksum("7890");

		Site identicalSite = new Site();
		identicalSite.setUrl(new URL("https://exe.com/index?parameter=this"));
		identicalSite.setChecksum("7890");

		Assert.assertEquals(HandlerResponse.GO_AHEAD, checker.handlePostParse(exampleSite));
		Assert.assertEquals(HandlerResponse.HALT, checker.handlePostParse(identicalSite));
		Assert
			.assertEquals(HandlerResponse.GO_AHEAD, checker.handlePreFetch(identicalSite.getUrl()));
	}

}
