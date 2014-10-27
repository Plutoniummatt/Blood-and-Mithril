package bloodandmithril.util.datastructure;

import java.io.Serializable;
import java.util.HashMap;

import bloodandmithril.core.Copyright;

/**
 * A concurrent two-dimensional HashMap
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ConcurrentDualKeyHashMap<X, Y, V> implements Serializable {
	private static final long serialVersionUID = -8864426466568336054L;

	/** The underlying data structue */
	private HashMap<X, HashMap<Y, V>> data = new HashMap<X, HashMap<Y, V>>();

	/**
	 * @return the value enquired upon
	 */
	public synchronized V get(X x, Y y) {
		return data.get(x) == null ? null : data.get(x).get(y);
	}


	/**
	 * Put an entry into this {@link ConcurrentDualKeyHashMap}
	 */
	public synchronized V put(X x, Y y, V v) {
		if (data.get(x) == null) {
			data.put(x, new HashMap<Y, V>());
		}
		return data.get(x).put(y, v);
	}


	/**
	 * Put an entry into this {@link ConcurrentDualKeyHashMap}
	 */
	public synchronized V remove(X x, Y y) {
		if (data.get(x) == null) {
			return null;
		}
		return data.get(x).remove(y);
	}
}
