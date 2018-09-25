package net.viperfish.crawler.html;

import java.net.URL;
import net.viperfish.crawler.core.ResourcesStream;

public interface HttpFetcher extends ResourcesStream<FetchedContent> {

	void submit(URL url);

	void setRestricitonManager(RestrictionManager mger);

	RestrictionManager getRestrictionManager();

}
