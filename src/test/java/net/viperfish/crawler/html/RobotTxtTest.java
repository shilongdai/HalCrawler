package net.viperfish.crawler.html;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import net.viperfish.crawler.html.restrictions.RobotTxt;
import org.junit.Assert;
import org.junit.Test;

public class RobotTxtTest {

	@Test
	public void testFullURL() throws MalformedURLException {
		List<String> disallowed = new LinkedList<>();
		disallowed.add("https://example.com/noCrawler.html");
		RobotTxt robotTxt = new RobotTxt(new URL("https://example.com"), new LinkedList<>(),
			disallowed, 0);

		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://example.com/noCrawler.html")).canFetch());
		Assert
			.assertTrue(robotTxt.isAllowed(new URL("https://example.com/crawler.html")).canFetch());
	}

	@Test
	public void testDisallowRoot() throws MalformedURLException {
		List<String> disallowed = new LinkedList<>();
		disallowed.add("/");
		URL baseURL = new URL("https://www.reddit.com");
		RobotTxt robotTxt = new RobotTxt(baseURL, new LinkedList<>(), disallowed, 0);

		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/index.html")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/testDir/index?param=true"))
				.canFetch());
	}

	@Test
	public void testDisallowWithExceptions() throws MalformedURLException {
		List<String> disallowed = new LinkedList<>();
		disallowed.add("/*");
		List<String> allowed = new LinkedList<>();
		allowed.add("/fish");
		URL baseURL = new URL("https://www.reddit.com");

		RobotTxt robotTxt = new RobotTxt(baseURL, allowed, disallowed, 0);

		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/randomFile.html")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/randomDirectory/randFile.html"))
				.canFetch());
		Assert
			.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish.html")).canFetch());
		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish")).canFetch());
		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish/")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/test.html")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/randomDirectory/rand.html"))
				.canFetch());
	}

	@Test
	public void testDisallowWildcard() throws MalformedURLException {
		List<String> disallowed = new LinkedList<>();
		disallowed.add("/fish*");
		disallowed.add("/test");
		URL baseURL = new URL("https://www.reddit.com");
		RobotTxt robotTxt = new RobotTxt(baseURL, new LinkedList<>(), disallowed, 0);

		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/randomFile")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish.html")).canFetch());
		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/fish")).canFetch());
		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/fish/")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/test.html")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/randomDirectory/rand.html"))
				.canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test.html")).canFetch());
		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/test")).canFetch());
		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/test/")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test/test2.html")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test/randomDirectory/rand.html"))
				.canFetch());
	}

	@Test
	public void testDisallowDirectory() throws MalformedURLException {
		List<String> disallowed = new LinkedList<>();
		disallowed.add("/fish/");
		URL baseURL = new URL("https://www.reddit.com");
		RobotTxt robotTxt = new RobotTxt(baseURL, new LinkedList<>(), disallowed, 0);

		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/randomFile")).canFetch());
		Assert
			.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish.html")).canFetch());
		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish")).canFetch());
		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/fish/")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/test.html")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/randomDirectory/rand.html"))
				.canFetch());
	}

	@Test
	public void testDisallowFileType() throws MalformedURLException {
		List<String> disallow = new LinkedList<>();
		disallow.add("/*.php");
		URL baseURL = new URL("https://www.reddit.com");
		RobotTxt robotTxt = new RobotTxt(baseURL, new LinkedList<>(), disallow, 0);

		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/randomFile/rand.txt")).canFetch());
		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish/")).canFetch());
		Assert
			.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/fish.cgi")).canFetch());

		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/index.php")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/randomDirectory/randomFile.php"))
				.canFetch());
		Assert
			.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/fish.php")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/fish/salmon.php?test=true"))
				.canFetch());
	}

	@Test
	public void testDisallowWithoutParam() throws MalformedURLException {
		List<String> disallow = new LinkedList<>();
		disallow.add("/test$");
		URL baseURL = new URL("https://www.reddit.com");
		RobotTxt robotTxt = new RobotTxt(baseURL, new LinkedList<>(), disallow, 0);

		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/someRandom")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test.php?testParam")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test/rand.cgi?test")).canFetch());

		Assert.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/test")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test.html")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test/random.html")).canFetch());
	}

	@Test
	public void testComposite() throws MalformedURLException {
		List<String> disallow = new LinkedList<>();
		disallow.add("/test*.php$");
		URL baseURL = new URL("https://www.reddit.com");
		RobotTxt robotTxt = new RobotTxt(baseURL, new LinkedList<>(), disallow, 0);

		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/someRandom")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test.php?testParam")).canFetch());
		Assert.assertTrue(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test/rand.cgi?test")).canFetch());
		Assert.assertTrue(robotTxt.isAllowed(new URL("https://www.reddit.com/test")).canFetch());

		Assert
			.assertFalse(robotTxt.isAllowed(new URL("https://www.reddit.com/test.php")).canFetch());
		Assert.assertFalse(
			robotTxt.isAllowed(new URL("https://www.reddit.com/test/random.php")).canFetch());
	}

}
