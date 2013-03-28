package de.learnlib.filters.reuse.api;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple implementation for a {@link SystemState} containing a
 * {@link java.util.Map} of key value pairs.
 *
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 * @see {@link SystemState}, {@link ReuseEdge}, {@link ReuseNode}, {@link ReuseTree}.
 */
public class SystemStateImpl implements SystemState {
	private Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public Object get(String key) {
		return map.get(key);
	}

	@Override
	public void put(String key, Object value) {
		map.put(key, value);
	}
}
