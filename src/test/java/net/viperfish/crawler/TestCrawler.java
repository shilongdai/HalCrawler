package net.viperfish.crawler;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.core.ORMLiteDatabase;
import net.viperfish.crawler.html.BaseHttpWebCrawler;
import net.viperfish.crawler.html.Site;
import net.viperfish.crawler.html.dao.AnchorDatabase;
import net.viperfish.crawler.html.dao.EmphasizedTextDatabase;
import net.viperfish.crawler.html.dao.HeaderDatabase;
import net.viperfish.crawler.html.dao.SiteDatabaseImpl;
import net.viperfish.crawler.html.dao.TextContentDatabase;
import net.viperfish.crawler.html.engine.ConcurrentHttpFetcher;
import net.viperfish.framework.compression.Compressor;
import net.viperfish.framework.compression.Compressors;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestCrawler {

	private static SiteDatabaseImpl siteDB;

	@BeforeClass
	public static void init() throws SQLException {
		ORMLiteDatabase.connect("jdbc:h2:mem:test", "testUser", "testPassword");
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

	}

	@AfterClass
	public static void cleanup() {
		ORMLiteDatabase.closeConn();
	}

	@Test
	public void testBasicCrawler()
		throws IOException, InterruptedException, NoSuchAlgorithmException {
		URL url2Test = new URL("https://example.com");
		BaseHttpWebCrawler crawler = new BaseHttpWebCrawler(siteDB,
			new ConcurrentHttpFetcher(1));
		crawler.submit(new URL("https://example.com/"));
		crawler.startCrawl();
		crawler.waitUntiDone();
		crawler.shutdown();

		Site crawled = siteDB.find(1L);

		String rawHTML = new String(IOUtil.read(url2Test.openStream()), StandardCharsets.UTF_8);
		byte[] htmlBytes = rawHTML.getBytes(StandardCharsets.UTF_16);
		Digest md5 = new MD5Digest();
		md5.update(htmlBytes, 0, htmlBytes.length);
		byte[] hashOut = new byte[16];
		md5.doFinal(hashOut, 0);
		Compressor compressor = Compressors.getCompressor("GZ");
		byte[] compressed = compressor.compress(htmlBytes);

		Assert.assertEquals(new URL("https://example.com/"), crawled.getUrl());
		Assert.assertArrayEquals(compressed, crawled.getCompressedHtml());
		Assert.assertEquals(Base64.getEncoder().encodeToString(hashOut), crawled.getChecksum());
	}
}
