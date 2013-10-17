package spritestar.util.datastructure;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

/**
 * A box whose position is defined as the middle of box
 * 
 * @author Matt
 */
public class Box implements Serializable {
	private static final long serialVersionUID = 13257061764372640L;

	/** Dimensions of the box */
	public float width, height;
	
	/** Position of the box */
	public Vector2 position;

	/**
	 * Constructor
	 */
	public Box(Vector2 position, float width, float height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}

	
	/** True if a location is within this {@link Box} */
	public boolean isWithinBox(Vector2 location) {
		return 
			location.x > position.x - width / 2 && 
			location.x < position.x + width / 2 && 
			location.y > position.y - height / 2 && 
			location.y < position.y + height / 2
		;
	}
}
