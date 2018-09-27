package net.viperfish.crawler.html;

import java.net.MalformedURLException;
import java.net.URL;
import net.viperfish.crawler.html.restrictions.RobotsTxtRestrictionManager;
import org.junit.Assert;
import org.junit.Test;

public class RobotTxtManagerTest {

	@Test
	public void testManager() throws MalformedURLException {
		RobotsTxtRestrictionManager manager = new RobotsTxtRestrictionManager("halbot");
		Restriction restriction1 = manager
			.getRestriction(new URL("https://www.viperfish.net/test"));
		Restriction restriction2 = manager
			.getRestriction(new URL("https://www.viperfish.net/wordpress"));

		Assert.assertFalse(restriction1.canFetch());
		Assert.assertTrue(restriction2.canFetch());
	}

}
