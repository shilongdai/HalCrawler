package net.viperfish.crawler.html.engine;

import java.net.URL;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The default implementation for the {@link PrioritizedURLBlockingQueue}. For this queue, the
 * higher the priority number, the greater the importance.
 */
public class DefaultPrioritizedURLBlockingQueue implements PrioritizedURLBlockingQueue {

	private static final int INITIAL_CAPACITY = 100;

	private PriorityBlockingQueue<PrioritizedURL> queue;
	private ConcurrentMap<URL, PrioritizedURL> urlTracker;

	/**
	 * creates an empty queue.
	 */
	public DefaultPrioritizedURLBlockingQueue() {
		queue = new PriorityBlockingQueue<>(INITIAL_CAPACITY, comparator());
		urlTracker = new ConcurrentHashMap<>();
	}

	@Override
	public void offer(URL url) {
		this.offer(new PrioritizedURL(url, 1));
	}

	@Override
	public void offer(PrioritizedURL prioritizedURL) {
		PrioritizedURL existing = urlTracker.get(prioritizedURL.getSource());
		if (existing == null) {
			if (urlTracker.putIfAbsent(prioritizedURL.getSource(), prioritizedURL) == null) {
				queue.offer(prioritizedURL);
			}
		} else {
			existing.increasePriority();
			if (queue.remove(existing)) {
				queue.offer(existing);
			}
		}
	}

	@Override
	public PrioritizedURL take() throws InterruptedException {
		PrioritizedURL result = queue.take();
		urlTracker.remove(result.getSource());
		return result;
	}

	@Override
	public PrioritizedURL take(int time, TimeUnit unit) throws InterruptedException {
		PrioritizedURL result = queue.poll(time, unit);
		if (result != null) {
			urlTracker.remove(result.getSource());
		}
		return result;
	}

	@Override
	public int size() {
		return queue.size();
	}

	/**
	 * gets a comparator that can be used to compare priorities.
	 *
	 * @return a priority comparator.
	 */
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
				if (o1.getSource().equals(o2.getSource())) {
					return 0;
				}
				return Integer.compare(o1.getSource().toExternalForm().length(),
					o2.getSource().toExternalForm().length());
			}
		}
	}
}
