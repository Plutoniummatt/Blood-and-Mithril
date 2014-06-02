package bloodandmithril.util;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Serializable} version of {@link Color}
 *
 * @author Matt
 */
public class SerializableColor extends Color implements Serializable {
	private static final long serialVersionUID = -6646062971215942066L;

	public SerializableColor(Color color) {
		super(color);
	}
}