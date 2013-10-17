package spritestar.util.datastructure;

import java.util.HashMap;

/**
 * @author Matt
 */
public class IntIntHashMap extends HashMap<String, String> {
	private static final long serialVersionUID = -3030362671166258486L;

	
	public Integer get(int key) {
		return super.get(Integer.toString(key)) == null ? null : Integer.parseInt(super.get(Integer.toString(key)));
	}

	
	public Integer put(int key, int value) {
		String answer = super.put(Integer.toString(key), Integer.toString(value));
		return answer == null ? 0 : Integer.parseInt(answer);
	}
	
	
	public Integer remove(int key) {
		return Integer.parseInt(super.remove(Integer.toString(key)));
	}
}