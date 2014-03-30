package bloodandmithril.world.topography.fluid;

import bloodandmithril.util.Util.Colors;

import com.badlogic.gdx.graphics.Color;

/**
 * Blood
 *
 * @author Matt
 */
public class Blood extends Fluid {
	private static final long serialVersionUID = 7813531583628229825L;
	
	private static Color color = Colors.DARK_RED;
	
	/**
	 * Constructor
	 */
	public Blood(float depth) {
		super(depth, color);
	}

	
	@Override
	protected Fluid internalClone(float depth) {
		return new Blood(depth);
	}
}