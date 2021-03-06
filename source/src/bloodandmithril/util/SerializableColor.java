package bloodandmithril.util;

import java.io.Serializable;

import bloodandmithril.core.Copyright;

import com.badlogic.gdx.graphics.Color;

/**
 * {@link Serializable} version of {@link Color}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class SerializableColor implements Serializable {
	private static final long serialVersionUID = -6646062971215942066L;

	public float r, g, b, a;

	public SerializableColor(Color color) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
	}
	
	
	public Color getColor() {
		return new Color(r, g, b, a);
	}
}