package bloodandmithril.world.weather;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;

/**
 * Class representing a celestial body
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public final class CelestialBody {

	/** Texture ID of this star */
	public final int textureId;

	/** Sky coordinates of the star, relative to the orbital pivot, angle is measured as at midnight, in degrees, positive clockwise */
	public final float orbitalRadius, angle;

	/** Color of this {@link CelestialBody} */
	public final Color filter;

	/** Whether this {@link CelestialBody} rotates */
	public final boolean rotates;

	/**
	 * Constructor
	 */
	public CelestialBody(int textureId, float orbitalRadius, float angle, Color filter, boolean rotates) {
		this.textureId = textureId;
		this.orbitalRadius = orbitalRadius;
		this.angle = angle;
		this.filter = filter;
		this.rotates = rotates;
	}
}