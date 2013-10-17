package spritestar.util.datastructure;

import java.util.HashMap;

/**
 * @author Matt
 */
public class ConcurrentIntIntToIntHashMap extends HashMap<String, IntIntHashMap> {
	private static final long serialVersionUID = 8972267916769479939L;

	public synchronized IntIntHashMap get(int key) {
		return super.get(Integer.toString(key)) == null ? null : super.get(Integer.toString(key));
	}
	
	public synchronized IntIntHashMap put(int key, IntIntHashMap value) {
		IntIntHashMap answer = super.put(Integer.toString(key), value);
		return answer == null ? null : answer;
	}
	
	public synchronized IntIntHashMap remove(int key) {
		return super.remove(Integer.toString(key));
	}
}
