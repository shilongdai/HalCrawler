package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DefaultPrioritizedURLBlockingQueue implements PrioritizedURLBlockingQueue {

	private static final int INITIAL_CAPACITY = 100;

	private PriorityBlockingQueue<PrioritizedURL> queue;
	private ConcurrentMap<URL, PrioritizedURL> urlTracker;

	public DefaultPrioritizedURLBlockingQueue() {
		queue = new PriorityBlockingQueue<>(INITIAL_CAPACITY, comparator());
		urlTracker = new ConcurrentHashMap<>();
	}

	@Override
	public void offer(URL url) {
		PrioritizedURL existing = urlTracker.get(url);
		if (existing == null) {
			PrioritizedURL newURL = new PrioritizedURL(url, 0);
			if (urlTracker.putIfAbsent(url, newURL) == null) {
				queue.offer(newURL);
			}
		} else {
			existing.increasePriority();
			if (queue.remove(existing)) {
				queue.offer(existing);
			}
		}
	}

	@Override
	public void offer(URL url, int priority) {
		queue.offer(new PrioritizedURL(url, priority));
	}

	@Override
	public PrioritizedURL take() throws InterruptedException {
		return queue.take();
	}

	@Override
	public PrioritizedURL take(int time, TimeUnit unit) throws InterruptedException {
		return queue.poll(time, unit);
	}

	@Override
	public int size() {
		return queue.size();
	}

	protected Comparator<PrioritizedURL> comparator() {
		return new DefaultComparator();
	}

	private static class DefaultComparator implements Comparator<PrioritizedURL> {

		@Override
		public int compare(PrioritizedURL o1, PrioritizedURL o2) {
			if (o1.getPriority() > o2.getPriority()) {
				return -1;
			} else if (o1.getPriority() < o2.getPriority()) {
				return 1;
			} else {
				if (o1.getToFetch().equals(o2.getToFetch())) {
					return 0;
				}
				return Integer.compare(o1.getToFetch().toExternalForm().length(),
					o2.getToFetch().toExternalForm().length());
			}
		}
	}
}
