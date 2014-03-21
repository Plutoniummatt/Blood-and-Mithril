package bloodandmithril.util.datastructure;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;

import com.google.common.collect.Lists;

public class DualKeyTreeMap<X, Y, V> implements Serializable {
	private static final long serialVersionUID = -3302956434707116279L;

	/** The underlying data structue */
	private TreeMap<X, TreeMap<Y, V>> data = new TreeMap<X, TreeMap<Y, V>>();
	
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
			data.put(x, new TreeMap<Y, V>());
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
	
	
	/**
	 * Returns a {@link Collection} of {@link DualKeyEntry}s contained within {@link #data}
	 */
	public List<DualKeyEntry<X, Y, V>> getAllEntries() {
		List<DualKeyEntry<X, Y, V>> entries = Lists.newLinkedList();
		
		for (Entry<X, TreeMap<Y, V>> outerEntry : data.entrySet()) {
			for (Entry<Y, V> innerEntry : outerEntry.getValue().entrySet()) {
				entries.add(new DualKeyEntry<X, Y, V>(outerEntry.getKey(), innerEntry.getKey(), innerEntry.getValue()));
			}
		}
		
		return entries;
	}
}
