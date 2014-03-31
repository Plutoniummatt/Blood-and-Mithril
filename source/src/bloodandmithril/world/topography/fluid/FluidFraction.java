package bloodandmithril.world.topography.fluid;

import java.io.Serializable;

import bloodandmithril.item.material.liquid.Liquid;

public class FluidFraction implements Serializable {
	private static final long serialVersionUID = 1071666818872722760L;
	
	private Liquid liquid;
	private float fraction;

	/**
	 * Private constructor
	 */
	private FluidFraction(Liquid liquid, float fraction) {
		this.liquid = liquid;
		this.fraction = fraction;
	}
	
	public static FluidFraction fluid(Liquid liquid, float fraction) {
		return new FluidFraction(liquid, fraction);
	}

	public Liquid getLiquid() {
		return liquid;
	}

	public void setLiquid(Liquid liquid) {
		this.liquid = liquid;
	}

	public float getFraction() {
		return fraction;
	}

	public void setFraction(float amount) {
		this.fraction = amount;
	}
}