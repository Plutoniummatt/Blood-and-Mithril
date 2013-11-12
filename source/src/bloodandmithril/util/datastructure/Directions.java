package bloodandmithril.util.datastructure;

import java.io.Serializable;

public class Directions implements Serializable {
	private static final long serialVersionUID = 7651314851840206122L;
	
	public boolean up;
	public boolean down;
	public boolean left;
	public boolean right;


	public Directions (boolean up, boolean down, boolean left, boolean right) {
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
	}
}
