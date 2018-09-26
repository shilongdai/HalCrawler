package net.viperfish.crawler.html;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.viperfish.crawler.core.IOUtil;

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
		if (robotTxts.containsKey(url.getProtocol() + url.getHost())) {
			RobotTxt robotTxt = robotTxts.get(url.getProtocol() + url.getHost());
			if (robotTxt == NULL_ROBOT_TXT) {
				return new UnrestrictedRestriction();
			}
			return robotTxt.isAllowed(url);
		}
		try {
			RobotTxt robotTxt = getRobotTxt(url);
			robotTxts.putIfAbsent(url.getProtocol() + url.getHost(), robotTxt);
			if (robotTxt == NULL_ROBOT_TXT) {
				return new UnrestrictedRestriction();
			}
			return robotTxt.isAllowed(url);
		} catch (IOException e) {
			e.printStackTrace();
			return new UnrestrictedRestriction();
		}
	}

	/**
	 * gets a {@link RobotTxt} from the base url of the specified url. If fetching failed, return
	 * the NULL_ROBOT_TXT.
	 *
	 * @param url the url from which the base url will be derived.
	 * @return the parsed {@link RobotTxt} or NULL_ROBOT_TXT if failed to fetch.
	 * @throws IOException if network error occurred.
	 */
	private RobotTxt getRobotTxt(URL url) throws IOException {
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
				RobotTxt parsed = parseRobotTxt(baseURL, robotStr);
				return parsed;
			} else {
				return NULL_ROBOT_TXT;
			}
		} finally {
			urlc.disconnect();
		}
	}

	// TODO: change the parsing algorithm so that the order of the robots.txt will not cause problem with the agent.

	/**
	 * parses the content of the robots.txt file. It will parse the section matching the agent
	 * specified.
	 *
	 * @param base the base url of the site
	 * @param robotStr the fetched robots.txt content
	 * @return the parsed robots.txt
	 */
	private RobotTxt parseRobotTxt(URL base, String robotStr) {
		List<String> allowed = new LinkedList<>();
		List<String> disallowed = new LinkedList<>();
		int crawlDelay = 0;

		String[] robotLines = robotStr.split("\n");
		boolean startParse = false;
		boolean alreadyParsed = false;
		for (String i : robotLines) {
			String[] fields = i.split(":");
			if (fields.length != 2) {
				continue;
			}
			String fieldName = fields[0].trim().toLowerCase();
			String fieldVal = fields[1].trim();
			if (fieldName.equals("user-agent")) {
				if (alreadyParsed) {
					break;
				}
				if (fieldVal.equals(userAgent) || fieldVal.equals("*")) {
					startParse = true;
					continue;
				} else {
					if (startParse) {
						alreadyParsed = true;
					}
					startParse = false;
				}
			}
			if (startParse) {
				switch (fieldName) {
					case "disallow": {
						disallowed.add(fieldVal);
						break;
					}
					case "allow": {
						allowed.add(fieldVal);
						break;
					}
					case "crawl-delay": {
						if (fieldVal.matches("\\d+")) {
							crawlDelay = Integer.parseInt(fieldVal);
						}
						break;
					}
				}
			}
		}
		return new RobotTxt(base, allowed, disallowed, crawlDelay);
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
}
