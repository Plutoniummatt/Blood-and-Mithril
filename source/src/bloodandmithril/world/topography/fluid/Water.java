package bloodandmithril.world.topography.fluid;

import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Fluid} that is the liquid of life...at least on earth
 *
 * @author Matt
 */
public class Water extends Fluid {

	private static Color color = Colors.modulateAlpha(Colors.WATER, 0.85f);
	
	/**
	 * Constructor
	 */
	public Water(int tileX, int tileY, int depth) {
		super(tileX, tileY, depth, color);
	}

	
	@Override
	protected Fluid internalClone() {
		return new Water(getTileX(), getTileY(), getDepth());
	}
}