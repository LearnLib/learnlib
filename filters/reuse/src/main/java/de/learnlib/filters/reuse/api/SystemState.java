package de.learnlib.filters.reuse.api;

/**
 * @author Oliver Bauer <oliver.bauer@tu-dortmund.de>
 */
public interface SystemState {
	/**
	 * Provides the referenced object for the given key or
	 * <code>null</code> if nothing stored under this key so far.
	 *
	 * @param key
	 * @return
	 */
	Object get(String key);

	/**
	 * Stores the key value pair.
	 *
	 * @param key
	 * @param value
	 */
	void put(String key, Object value);
}
