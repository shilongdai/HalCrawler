package net.viperfish.crawler.html.restrictions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.viperfish.crawler.core.IOUtil;
import net.viperfish.crawler.html.Restriction;
import net.viperfish.crawler.html.RestrictionManager;

/**
 * A {@link RestrictionManager} that creates {@link Restriction}s based on a Site's robots.txt. This
 * class follows the convention for robots.txt as specified by Google's guide. However, it does
 * respect crawl-delay. It will try to find a robots.txt file under the root directory of the site,
 * and fetch it. If the fetching fails, it will assume that nothing is restricted. Then, this class
 * will parse the section under the specified user-agent, or wildcard if no user-agent matching this
 * user-agent is found. This class is thread safe.
 */
public class RobotsTxtRestrictionManager implements RestrictionManager {

	/**
	 * A reference representing a null RobotTxt
	 */
	private static final RobotTxt NULL_ROBOT_TXT;

	static {
		try {
			NULL_ROBOT_TXT = new RobotTxt(new URL("https://null.com"), new LinkedList<>(),
				new LinkedList<>(), 0);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private ConcurrentMap<String, RobotTxt> robotTxts;
	private String userAgent;

	/**
	 * creates a new instance with specified userAgent
	 */
	public RobotsTxtRestrictionManager(String userAgent) {
		robotTxts = new ConcurrentHashMap<>();
		this.userAgent = userAgent;
	}

	@Override
	public Restriction getRestriction(URL url) {
		RobotTxt robotTxt = getRobotsTxt(url);
		return robotTxt.isAllowed(url);

	}

	/**
	 * gets a {@link RobotTxt} by either fetching it online or pulling it from the cache.
	 *
	 * @param url the URL to check
	 * @return a fetched robots.txt
	 */
	private RobotTxt getRobotsTxt(URL url) {
		if (robotTxts.containsKey(url.getProtocol() + url.getHost())) {
			return robotTxts.get(url.getProtocol() + url.getHost());
		}
		RobotTxt fetched = null;
		try {
			fetched = fetchRobotsTxt(url);
		} catch (IOException e) {
			System.out.println("Failed to get robots.txt:" + e.getMessage());
			fetched = NULL_ROBOT_TXT;
		} finally {
			robotTxts.putIfAbsent(url.getProtocol() + url.getHost(), fetched);
		}
		return fetched;
	}

	/**
	 * fetch a {@link RobotTxt} from the base url of the specified url. If fetching failed, return
	 * the NULL_ROBOT_TXT.
	 *
	 * @param url the url from which the base url will be derived.
	 * @return the parsed {@link RobotTxt} or NULL_ROBOT_TXT if failed to fetch.
	 * @throws IOException if network error occurred.
	 */
	private RobotTxt fetchRobotsTxt(URL url) throws IOException {
		URL baseURL = getBaseURL(url);
		URL robotsTxtURL = new URL(baseURL.toExternalForm() + "/robots.txt");
		HttpURLConnection urlc = (HttpURLConnection) robotsTxtURL.openConnection();
		// fetch robot.txt
		urlc.setRequestMethod("GET");
		urlc.setRequestProperty("User-Agent",
			userAgent);
		try {
			urlc.connect();
			if (urlc.getResponseCode() < 300 && urlc.getResponseCode() > 199) {
				String robotStr = new String(IOUtil.read(urlc.getInputStream()),
					StandardCharsets.UTF_8);
				RobotTxt result = applySection(baseURL, userAgent, robotStr);
				if (result == NULL_ROBOT_TXT) {
					result = applySection(baseURL, "*", robotStr);
				}
				return result;
			}
		} finally {
			urlc.disconnect();
		}
		return NULL_ROBOT_TXT;
	}

	/**
	 * gets the base url from the given url
	 *
	 * @param url the url to convert to base
	 * @return the base url
	 * @throws MalformedURLException if the format is wrong.
	 */
	private URL getBaseURL(URL url) throws MalformedURLException {
		String baseStr = url.getProtocol() + "://" + url.getHost() + "/";
		return new URL(baseStr);
	}

	/**
	 * gets a specific section of the robots.txt as the {@link RobotTxt} object based on the
	 * user-agent.
	 *
	 * @param base the base url of the site.
	 * @param userAgent the user-agent to match
	 * @param robotTxtContent the content of the actual robots.txt
	 * @return whether any section matched.
	 */
	private RobotTxt applySection(URL base, String userAgent,
		String robotTxtContent) {
		String[] lines = robotTxtContent.split("\n");
		boolean startParse = false;
		boolean matched = false;
		List<String> allowed = new LinkedList<>();
		List<String> disallowed = new LinkedList<>();
		AtomicInteger crawlDelay = new AtomicInteger(0);
		for (String l : lines) {
			// remove all control characters
			l = l.replaceAll("\\p{Cntrl}", "");
			String[] parameters = l.split(":");
			if (parameters.length != 2) {
				continue;
			}
			String key = parameters[0];
			String value = parameters[1];
			if (key.trim().equalsIgnoreCase("user-agent")) {
				if (value.trim().equals(userAgent)) {
					startParse = true;
					matched = true;
				} else {
					startParse = false;
				}
			}
			if (startParse) {
				parseParameters(allowed, disallowed, crawlDelay, key, value);
			}
		}
		if (matched) {
			return new RobotTxt(base, allowed, disallowed, crawlDelay.get());
		}
		return NULL_ROBOT_TXT;
	}

	/**
	 * fills in the fields of the {@link RobotTxt} based on the key value pair in robots.txt
	 *
	 * @param allowed the allowed list
	 * @param disallowed the disallowed list
	 * @param crawlDelay the crawl delay
	 * @param key the key of the pair
	 * @param value the value of the pair.
	 */
	private void parseParameters(List<String> allowed, List<String> disallowed,
		AtomicInteger crawlDelay, String key, String value) {
		key = key.trim().toLowerCase();
		value = value.trim();
		switch (key) {
			case "disallow": {
				disallowed.add(value);
				break;
			}
			case "allow": {
				allowed.add(value);
				break;
			}
			case "crawl-delay": {
				if (value.matches("\\d+")) {
					crawlDelay.set(Integer.parseInt(value));
				}
				break;
			}
		}
	}

}
