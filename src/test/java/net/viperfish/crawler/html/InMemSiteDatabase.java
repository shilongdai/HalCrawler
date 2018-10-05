package net.viperfish.crawler.html;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.viperfish.crawler.core.Datasink;

public class InMemSiteDatabase implements Datasink<CrawledData> {

	private ConcurrentMap<URL, CrawledData> data;
	private boolean isClosed;

	@Override
	public void init() {
		data = new ConcurrentHashMap<>();
		isClosed = false;
	}

	@Override
	public void write(CrawledData data) {
		this.data.putIfAbsent(data.getUrl(), data);
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public void close() {
		isClosed = true;
		data.clear();
	}

	public CrawledData getOrDefault(Object key, CrawledData defaultValue) {
		return data.getOrDefault(key, defaultValue);
	}

	public void forEach(
		BiConsumer<? super URL, ? super CrawledData> action) {
		data.forEach(action);
	}

	public CrawledData putIfAbsent(URL key, CrawledData value) {
		return data.putIfAbsent(key, value);
	}

	public boolean remove(Object key, Object value) {
		return data.remove(key, value);
	}

	public boolean replace(URL key, CrawledData oldValue,
		CrawledData newValue) {
		return data.replace(key, oldValue, newValue);
	}

	public CrawledData replace(URL key, CrawledData value) {
		return data.replace(key, value);
	}

	public void replaceAll(
		BiFunction<? super URL, ? super CrawledData, ? extends CrawledData> function) {
		data.replaceAll(function);
	}

	public CrawledData computeIfAbsent(URL key,
		Function<? super URL, ? extends CrawledData> mappingFunction) {
		return data.computeIfAbsent(key, mappingFunction);
	}

	public CrawledData computeIfPresent(URL key,
		BiFunction<? super URL, ? super CrawledData, ? extends CrawledData> remappingFunction) {
		return data.computeIfPresent(key, remappingFunction);
	}

	public CrawledData compute(URL key,
		BiFunction<? super URL, ? super CrawledData, ? extends CrawledData> remappingFunction) {
		return data.compute(key, remappingFunction);
	}

	public CrawledData merge(URL key, CrawledData value,
		BiFunction<? super CrawledData, ? super CrawledData, ? extends CrawledData> remappingFunction) {
		return data.merge(key, value, remappingFunction);
	}

	public int size() {
		return data.size();
	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	public CrawledData get(Object key) {
		return data.get(key);
	}

	public CrawledData put(URL key, CrawledData value) {
		return data.put(key, value);
	}

	public CrawledData remove(Object key) {
		return data.remove(key);
	}

	public void putAll(
		Map<? extends URL, ? extends CrawledData> m) {
		data.putAll(m);
	}

	public void clear() {
		data.clear();
	}

	public Set<URL> keySet() {
		return data.keySet();
	}

	public Collection<CrawledData> values() {
		return data.values();
	}

	public Set<Entry<URL, CrawledData>> entrySet() {
		return data.entrySet();
	}
}
