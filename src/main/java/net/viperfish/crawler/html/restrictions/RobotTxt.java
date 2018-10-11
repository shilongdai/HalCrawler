package net.viperfish.crawler.html.restrictions;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.viperfish.crawler.html.Restriction;

/**
 * A representation of the robots.txt file with utilities to match urls against the url patterns.
 */
public class RobotTxt {

	private URL baseURL;
	private List<String> allowed;
	private List<String> disallowed;
	private int crawlDelay;
	private List<Pattern> disallowedPattern;
	private List<Pattern> allowedPattern;

	/**
	 * creates a new RobotTxt with the specified values.
	 *
	 * @param baseURL the base url of the site, i.e. https://www.oracle.com
	 * @param allowed the urls allowed by the robots.txt
	 * @param disallowed the urls disallowed by the robots.txt
	 * @param crawlDelay the numerical delays in between requests in seconds
	 */
	public RobotTxt(URL baseURL, List<String> allowed, List<String> disallowed, int crawlDelay) {
		this.allowed = Collections.unmodifiableList(new LinkedList<>(allowed));
		this.disallowed = Collections.unmodifiableList(new LinkedList<>(disallowed));
		this.baseURL = baseURL;
		this.crawlDelay = crawlDelay;

		createPatterns();
	}

	/**
	 * gets the allowed urls.
	 *
	 * @return the allowed urls.
	 */
	public List<String> getAllowed() {
		return allowed;
	}

	/**
	 * gets the disallowed urls
	 *
	 * @return the disallowed urls.
	 */
	public List<String> getDisallowed() {
		return disallowed;
	}

	/**
	 * gets the crawl delays between requests
	 *
	 * @return the crawl delay
	 */
	public int getCrawlDelay() {
		return crawlDelay;
	}

	/**
	 * tests whether a given url is allowed based on the allowed list and the disallowed list. This
	 * implementation uses Google's standard where allowed can override disallowed.
	 *
	 * @param url the url to check
	 * @return a restriction representing the permissions regarding the specified URL.
	 */
	public Restriction isAllowed(URL url) {
		boolean disallowed = checkDisallowed(url);
		boolean allowed = checkAllowed(url);

		boolean finalVerdict = !disallowed || allowed;
		return new BasicRestriction(finalVerdict, finalVerdict);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RobotTxt robotTxt = (RobotTxt) o;
		return crawlDelay == robotTxt.crawlDelay &&
			Objects.equals(allowed, robotTxt.allowed) &&
			Objects.equals(disallowed, robotTxt.disallowed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allowed, disallowed, crawlDelay);
	}


	/**
	 * checks if the specified URL matches any disallowed urls.
	 *
	 * @param toTest the URL to test
	 * @return true if matches false otherwise.
	 */
	private boolean checkDisallowed(URL toTest) {
		for (Pattern p : disallowedPattern) {
			Matcher match = p.matcher(toTest.toExternalForm());
			if (match.find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if the specified URL matches any allowed URLs
	 *
	 * @param toTest the URL to test
	 * @return true if matches false otherwise.
	 */
	private boolean checkAllowed(URL toTest) {
		for (Pattern p : allowedPattern) {
			Matcher match = p.matcher(toTest.toExternalForm());
			if (match.find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * generates the Java regex patterns based on the robots.txt patterns.
	 *
	 * @param str the URL pattern
	 * @return a regex pattern representing the URL pattern.
	 */
	private Pattern generatePattern(String str) {
		if (str.startsWith("http:") || str.startsWith("https")) {
			return Pattern.compile(str);
		}
		String baseURLString = baseURL.toExternalForm();
		if (baseURLString.endsWith("/")) {
			baseURLString = baseURLString.substring(0, baseURLString.length() - 1);
		}
		if (!(str.equals("/") || str.equals("/*"))) {
			str = baseURLString + str;
		} else {
			str = baseURLString;
		}
		str = quoteNonWildcard(str);
		if (str.indexOf("*") == -1) {
			if (str.endsWith("$")) {
				str = str.substring(0, str.length() - 1) + "*$";
			} else {
				str += "*";
			}
		}
		str = str.replaceAll("\\*", "(\\\\S+)?");
		if (str.endsWith("$")) {
			str = "(?!\\S+\\?)" + str.substring(0, str.length() - 1);
		}
		return Pattern.compile(str);
	}

	/**
	 * generates a regex pattern for each URL pattern in the allowed and disallowed lists.
	 */
	private void createPatterns() {
		allowedPattern = new LinkedList<>();
		disallowedPattern = new LinkedList<>();

		for (String disallow : disallowed) {
			disallowedPattern.add(generatePattern(disallow));
		}
		for (String allow : allowed) {
			allowedPattern.add(generatePattern(allow));
		}
	}

	/**
	 * pre-processes the url pattern before converting regex to make processing easier. It escapes
	 * all the special characters in the non-special parts of the URL pattern, and pieces the
	 * pattern back together with special pattern characters.
	 *
	 * @param str the pattern to escape
	 * @return the escaped pattern.
	 */
	private String quoteNonWildcard(String str) {
		boolean endsInWildCard = str.endsWith("*");
		boolean beginsWithWildCard = str.startsWith("*");
		boolean endsWithDollarSign = str.endsWith("$");
		if (endsWithDollarSign) {
			str = str.substring(0, str.length() - 1);
		}
		String[] segments = str.split("\\*");
		String[] quotedSegments = new String[segments.length];
		for (int i = 0; i < segments.length; ++i) {
			quotedSegments[i] = Pattern.quote(segments[i]);
		}
		StringBuilder sb = new StringBuilder();
		for (String i : quotedSegments) {
			sb.append(i).append("*");
		}
		if (!endsInWildCard) {
			sb.deleteCharAt(sb.length() - 1);
		}
		if (beginsWithWildCard) {
			sb.insert(0, "*");
		}
		if (endsWithDollarSign) {
			sb.append("$");
		}
		return sb.toString();
	}

}
