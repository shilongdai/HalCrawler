package net.viperfish.crawler.html.engine;

import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Assert;
import org.junit.Test;

public class TestPrioritizedURLQueue {

	@Test
	public void testPriority() throws MalformedURLException, InterruptedException {
		PrioritizedURLBlockingQueue queue = new DefaultPrioritizedURLBlockingQueue();
		URL least = new URL("https://www.least.com");
		URL second = new URL("https://www.second.com");
		URL first = new URL("https://www.first.com");

		for (int i = 0; i < 2; ++i) {
			queue.offer(least);
		}

		for (int i = 0; i < 5; ++i) {
			queue.offer(second);
		}

		for (int i = 0; i < 100; ++i) {
			queue.offer(first);
		}

		Assert.assertEquals(3, queue.size());
		Assert.assertEquals(first, queue.take().getToFetch());
		Assert.assertEquals(second, queue.take().getToFetch());
		Assert.assertEquals(least, queue.take().getToFetch());
	}

}
