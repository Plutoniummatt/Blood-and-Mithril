package bloodandmithril.util.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


/**
 * An easy two-dimensional HashMap
 *
 * @author Matt
 */
public class DualKeyHashMap<X, Y, V> implements Serializable {
	private static final long serialVersionUID = 200088372379639439L;
	
	/** The underlying data structue */
	private HashMap<X, HashMap<Y, V>> data = new HashMap<X, HashMap<Y, V>>();
	
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
			data.put(x, new HashMap<Y, V>());
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
		List<DualKeyEntry<X, Y, V>> entries = new ArrayList<DualKeyEntry<X, Y, V>>();
		
		for (Entry<X, HashMap<Y, V>> outerEntry : data.entrySet()) {
			for (Entry<Y, V> innerEntry : outerEntry.getValue().entrySet()) {
				entries.add(new DualKeyEntry<X, Y, V>(outerEntry.getKey(), innerEntry.getKey(), innerEntry.getValue()));
			}
		}
		
		return entries;
	}
	
	
	/**
	 * Dual key version of a java util map entry.
	 * 
	 * @author Matt
	 */
	public static class DualKeyEntry<X, Y, V> {
		public X x;
		public Y y;
		public V value;
		
		/**
		 * Constructor
		 */
		public DualKeyEntry(X x, Y y, V value) {
			this.x = x;
			this.y = y;
			this.value = value;
		}
	}
}
