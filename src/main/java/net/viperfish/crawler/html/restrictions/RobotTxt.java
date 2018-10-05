package net.viperfish.crawler.html.restrictions;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.viperfish.crawler.html.Restriction;

public class RobotTxt {

	private URL baseURL;
	private List<String> allowed;
	private List<String> disallowed;
	private int crawlDelay;
	private List<Pattern> disallowedPattern;
	private List<Pattern> allowedPattern;

	public RobotTxt(URL baseURL, List<String> allowed, List<String> disallowed, int crawlDelay) {
		this.allowed = Collections.unmodifiableList(new LinkedList<>(allowed));
		this.disallowed = Collections.unmodifiableList(new LinkedList<>(disallowed));
		this.baseURL = baseURL;
		this.crawlDelay = crawlDelay;

		createPatterns();
	}

	public List<String> getAllowed() {
		return allowed;
	}

	public List<String> getDisallowed() {
		return disallowed;
	}

	public int getCrawlDelay() {
		return crawlDelay;
	}

	public Restriction isAllowed(URL url) {
		for (Pattern p : disallowedPattern) {
			Matcher disallowMatch = p.matcher(url.toExternalForm());
			if (disallowMatch.find()) {
				for (Pattern a : allowedPattern) {
					Matcher allowMatch = a.matcher(url.toExternalForm());
					if (allowMatch.find()) {
						return new RestrictionAdapter(true);
					}
				}
				return new RestrictionAdapter(false);
			}
		}
		return new RestrictionAdapter(true);
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

	private static class RestrictionAdapter implements Restriction {

		private boolean isAllowed;

		RestrictionAdapter(boolean isAllowed) {
			this.isAllowed = isAllowed;
		}

		@Override
		public boolean canIndex() {
			return isAllowed;
		}

		@Override
		public boolean canFetch() {
			return isAllowed;
		}
	}

}