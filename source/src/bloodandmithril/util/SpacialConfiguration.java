package bloodandmithril.util;

import com.badlogic.gdx.math.Vector2;

/**
 * Position and orientation
 *
 * @author Matt
 */
public class SpacialConfiguration {
	
	public Vector2 position;
	public float orientation;
	public boolean flipX;
	
	/**
	 * Constructor
	 */
	public SpacialConfiguration(Vector2 position, float orientation, boolean flipX) {
		this.position = position;
		this.orientation = orientation;
		this.flipX = flipX;
	}
}