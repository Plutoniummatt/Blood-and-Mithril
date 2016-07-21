package bloodandmithril.util.datastructure;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.ui.UserInterface;

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
	public Box(final Vector2 position, final float width, final float height) {
		this.position = position;
		this.width = width;
		this.height = height;
	}


	/** True if a location is within this {@link Box} */
	public boolean isWithinBox(final Vector2 location) {
		return isWithinBox(location.x, location.y);
	}


	/** True if a location is within this {@link Box} */
	public boolean isWithinBox(final float x, final float y) {
		return
			x > position.x - width / 2 &&
			x < position.x + width / 2 &&
			y > position.y - height / 2 &&
			y < position.y + height / 2
		;
	}


	public boolean overlapsWith(final Box another) {
		final float left = position.x - width / 2;
		final float right = position.x + width / 2;
		final float top = position.y + height / 2;
		final float bottom = position.y - height / 2;

		final float otherLeft = another.position.x - another.width / 2;
		final float otherRight = another.position.x + another.width / 2;
		final float otherTop = another.position.y + another.height / 2;
		final float otherBottom = another.position.y - another.height / 2;

	    if (right < otherLeft)
		 {
			return false; // a is left of b
		}
	    if (left > otherRight)
		 {
			return false; // a is right of b
		}
	    if (top < otherBottom)
		 {
			return false; // a is above b
		}
	    if (bottom > otherTop)
		 {
			return false; // a is below b
		}

	    return true; // boxes overlap
	}


	public void render() {
		Wiring.injector().getInstance(UserInterface.class).getShapeRenderer().rect(
			position.x - width / 2,
			position.y - height / 2,
			width,
			height
		);
	}
}
