package net.viperfish.crawler.core;

import java.util.HashMap;
import java.util.Map;

public final class TagData {

	private TagDataType dataType;
	private Map<String, Object> properties;

	public TagData() {
		properties = new HashMap<>();
	}

	public TagData(TagDataType dataType) {
		this();
		this.dataType = dataType;
	}

	public TagDataType getDataType() {
		return dataType;
	}

	public void setDataType(TagDataType dataType) {
		this.dataType = dataType;
	}

	public <T> void set(String propertyName, T data) {
		properties.put(propertyName, data);
	}

	public <T> T get(String propertyName, Class<? extends T> type) {
		return type.cast(properties.get(propertyName));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TagData other = (TagData) obj;
		if (dataType != other.dataType) {
			return false;
		}
		if (properties == null) {
			return other.properties == null;
		} else {
			return properties.equals(other.properties);
		}
	}

	@Override
	public String toString() {
		return "TagData{" +
			"dataType=" + dataType +
			", properties=" + properties +
			'}';
	}
}
