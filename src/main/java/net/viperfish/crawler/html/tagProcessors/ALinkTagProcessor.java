package net.viperfish.crawler.html.tagProcessors;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.viperfish.crawler.base.TagProcessor;
import net.viperfish.crawler.core.Site;
import net.viperfish.crawler.core.TagData;
import net.viperfish.crawler.core.TagDataType;
import net.viperfish.crawler.exceptions.ParsingException;
import org.jsoup.nodes.Element;

public final class ALinkTagProcessor implements TagProcessor {

	@Override
	public Map<TagDataType, List<TagData>> processTag(Element tag, Site site)
		throws ParsingException {

		Map<TagDataType, List<TagData>> result = new HashMap<>();
		result.put(TagDataType.HTML_LINK, new LinkedList<>());

		String url = tag.attr("href");
		try {
			if (isRelative(url)) {
				if (url.startsWith("/")) {
					url = parseRelativeDirectly2Host(site.getUrl(), url);
				} else {
					URL nearestDir = getNearestPath(site.getUrl());
					url = new URL(nearestDir, url).toString();
				}
			}
			TagData tagParsed = new TagData(TagDataType.HTML_LINK);
			tagParsed.set("url", new URL(url));
			tagParsed.set("anchor", tag.text());
			result.get(TagDataType.HTML_LINK).add(tagParsed);
		} catch (URISyntaxException | MalformedURLException e) {
			throw new ParsingException(e);
		}
		return result;
	}

	@Override
	public boolean shouldProcess(Element e) {
		return e.text() != null && !e.text().isEmpty();
	}

	private boolean isRelative(String url) throws URISyntaxException {
		return !(url.startsWith("http://") || url.startsWith("https://"));
	}

	private String parseRelativeDirectly2Host(URL base, String url) {
		StringBuilder sb = new StringBuilder(base.getProtocol());
		sb.append("://").append(base.getHost()).append(url);
		return sb.toString();
	}

	private URL getNearestPath(URL base) throws MalformedURLException {
		URL url = new URL(
			base.toExternalForm().substring(0, base.toExternalForm().lastIndexOf("/") + 1));
		return url;
	}

}
