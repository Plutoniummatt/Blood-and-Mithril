package bloodandmithril.character.ai.perception;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.math.Vector2;

/**
 * The ability to see
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Observer {

	public default void observe() {
		Vector2 eyes = getObservationPosition();
	}

	/**
	 * @return the position of the eyes
	 */
	public Vector2 getObservationPosition();

	/**
	 * @return the direction the eyes are facing
	 */
	public Vector2 getDirection();

	/**
	 * @return the FOV, in degrees
	 */
	public Vector2 getFieldOfView();

	/**
	 * @return the maximum view distance
	 */
	public float getViewDistance();
}