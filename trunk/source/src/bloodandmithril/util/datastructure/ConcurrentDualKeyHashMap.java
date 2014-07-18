package bloodandmithril.util.datastructure;

import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.core.Copyright;

/**
 * A concurrent two-dimensional HashMap
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ConcurrentDualKeyHashMap<X, Y, V> {

	/** The underlying data structue */
	private ConcurrentHashMap<X, ConcurrentHashMap<Y, V>> data = new ConcurrentHashMap<X, ConcurrentHashMap<Y, V>>();

	/**
	 * @return the value enquired upon
	 */
	public V get(X x, Y y) {
		return data.get(x) == null ? null : data.get(x).get(y);
	}


	/**
	 * Put an entry into this {@link ConcurrentDualKeyHashMap}
	 */
	public V put(X x, Y y, V v) {
		if (data.get(x) == null) {
			data.put(x, new ConcurrentHashMap<Y, V>());
		}
		return data.get(x).put(y, v);
	}


	/**
	 * Put an entry into this {@link ConcurrentDualKeyHashMap}
	 */
	public V remove(X x, Y y) {
		if (data.get(x) == null) {
			return null;
		}
		return data.get(x).remove(y);
	}
}
