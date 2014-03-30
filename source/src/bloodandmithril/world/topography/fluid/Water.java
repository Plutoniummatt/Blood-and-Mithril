package bloodandmithril.world.topography.fluid;

import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Fluid} that is the liquid of life...at least on earth
 *
 * @author Matt
 */
public class Water extends Fluid {
	private static final long serialVersionUID = 6655685059114633586L;
	
	private static Color color = Colors.modulateAlpha(Colors.WATER, 0.85f);
	
	/**
	 * Constructor
	 */
	public Water(float depth) {
		super(depth, color);
	}

	
	@Override
	protected Fluid internalClone(float depth) {
		return new Water(depth);
	}
}