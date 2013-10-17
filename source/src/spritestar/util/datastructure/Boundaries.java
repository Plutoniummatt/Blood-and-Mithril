package spritestar.util.datastructure;

import java.io.Serializable;

/**
 * Stores 4 ints to be used as boundaries for a rectangle.
 *
 * @author sam
 */
public class Boundaries implements Serializable {
	private static final long serialVersionUID = -4985478591963928423L;
	
	public int top;
	public int bottom;
	public int left;
	public int right;

	public Boundaries(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}
}
