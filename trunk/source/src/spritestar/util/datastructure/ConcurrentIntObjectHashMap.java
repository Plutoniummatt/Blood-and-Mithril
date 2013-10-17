package spritestar.util.datastructure;

import java.util.HashMap;

/**
 * @author Matt
 */
public class ConcurrentIntObjectHashMap<K> extends HashMap<String, K> {
	private static final long serialVersionUID = -2579278799768569264L;


	public synchronized K get(int key) {
		return super.get(Integer.toString(key)) == null ? null : super.get(Integer.toString(key));
	}


	public synchronized K put(int key, K value) {
		return super.put(Integer.toString(key), value);
	}


	public synchronized K remove(int key) {
		return super.remove(Integer.toString(key));
	}
}
