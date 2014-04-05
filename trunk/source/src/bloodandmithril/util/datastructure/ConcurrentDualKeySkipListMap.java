package bloodandmithril.util.datastructure;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;

import com.google.common.collect.Lists;

/**
 * 2D version of a {@link ConcurrentSkipListMap}
 * 
 * @author Matt
 */
public class ConcurrentDualKeySkipListMap<X extends Serializable, Y extends Serializable, V extends Serializable> implements Serializable {
	private static final long serialVersionUID = -3302956434707116279L;

	/** The underlying data structue */
	private ConcurrentSkipListMap<X, ConcurrentSkipListMap<Y, V>> data = new ConcurrentSkipListMap<X, ConcurrentSkipListMap<Y, V>>();
	
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
			data.put(x, new ConcurrentSkipListMap<Y, V>());
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
		
		for (Entry<X, ConcurrentSkipListMap<Y, V>> outerEntry : data.entrySet()) {
			for (Entry<Y, V> innerEntry : outerEntry.getValue().entrySet()) {
				entries.add(new DualKeyEntry<X, Y, V>(outerEntry.getKey(), innerEntry.getKey(), innerEntry.getValue()));
			}
		}
		
		return entries;
	}
}
