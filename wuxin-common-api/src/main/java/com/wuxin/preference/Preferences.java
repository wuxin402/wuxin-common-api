package com.wuxin.preference;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Preferences {
	private String name;
	private String description;
	private Map<String, Object> items = new HashMap<>();

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Object get(String key) {
		return this.items.get(key);
	}

	public Object put(String key, Object value) {
		return this.items.put(key, value);
	}

	public boolean containsKey(String key) {
		return this.items.containsKey(key);
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return this.items.entrySet();
	}

	public String toString() {
		return this.items.toString();
	}
}