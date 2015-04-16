package bloodandmithril.util.datastructure;

import static bloodandmithril.ui.UserInterface.shapeRenderer;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.math.Vector2;

/**
 * A box whose position is defined as the centre of box
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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
		return isWithinBox(location.x, location.y);
	}


	/** True if a location is within this {@link Box} */
	public boolean isWithinBox(float x, float y) {
		return
			x > position.x - width / 2 &&
			x < position.x + width / 2 &&
			y > position.y - height / 2 &&
			y < position.y + height / 2
		;
	}


	public boolean overlapsWith(Box another) {
		float left = position.x - width / 2;
		float right = position.x + width / 2;
		float top = position.y + height / 2;
		float bottom = position.y - height / 2;

		float otherLeft = another.position.x - another.width / 2;
		float otherRight = another.position.x + another.width / 2;
		float otherTop = another.position.y + another.height / 2;
		float otherBottom = another.position.y - another.height / 2;

	    if (right < otherLeft) return false; // a is left of b
	    if (left > otherRight) return false; // a is right of b
	    if (top < otherBottom) return false; // a is above b
	    if (bottom > otherTop) return false; // a is below b

	    return true; // boxes overlap
	}


	public void render() {
		shapeRenderer.rect(
			position.x - width / 2,
			position.y - height / 2,
			width,
			height
		);
	}
}
