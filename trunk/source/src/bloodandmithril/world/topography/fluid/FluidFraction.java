package bloodandmithril.world.topography.fluid;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.liquid.Liquid;

@Copyright("Matthew Peck 2014")
public class FluidFraction implements Serializable {
	private static final long serialVersionUID = 1071666818872722760L;

	private Liquid liquid;
	private float depth;

	/**
	 * Private constructor
	 */
	private FluidFraction(Liquid liquid, float depth) {
		this.liquid = liquid;
		this.depth = depth;
	}

	public static <T extends Liquid> FluidFraction fraction(T liquid, float fraction) {
		return new FluidFraction(liquid, fraction);
	}

	public Liquid getLiquid() {
		return liquid;
	}

	public void setLiquid(Liquid liquid) {
		this.liquid = liquid;
	}

	public float getDepth() {
		return depth;
	}

	public void setDepth(float depth) {
		this.depth = depth;
	}
}