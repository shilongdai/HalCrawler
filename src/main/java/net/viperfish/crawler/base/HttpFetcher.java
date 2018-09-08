package net.viperfish.crawler.base;

import java.io.IOException;
import java.net.URL;

public interface HttpFetcher {

	void submit(URL url);

	FetchedContent next() throws IOException;

	void shutdown();

	boolean isShutdown();
}
