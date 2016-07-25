package bloodandmithril.generation;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * A utility class that places props, used in generation
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class PropPlacement implements Serializable {
	private static final long serialVersionUID = 578683865977423061L;
	public final Prop prop;
	public final Vector2 location;
	public final int worldId;

	/**
	 * Constructor
	 */
	public PropPlacement(Prop prop, Vector2 location, int worldId) {
		this.prop = prop;
		this.location = location;
		this.worldId = worldId;
	}
}