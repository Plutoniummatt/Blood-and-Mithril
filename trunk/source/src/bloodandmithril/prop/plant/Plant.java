package bloodandmithril.prop.plant;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;

/**
 * A Plant {@link Prop}
 *
 * @author Matt
 */
public abstract class Plant extends Harvestable {
	private static final long serialVersionUID = -4865430066854382581L;
	
	/** Dimensions of this {@link Plant} */
	protected final int width, height;

	/**
	 * Constructor
	 */
	protected Plant(float x, float y, int width, int height) {
		super(x, y, true);
		this.width = width;
		this.height = height;
	}


	@Override
	public boolean isMouseOver() {
		float mx = BloodAndMithrilClient.getMouseWorldX();
		float my = BloodAndMithrilClient.getMouseWorldY();

		return mx > position.x - width/2 && mx < position.x + width/2 && my > position.y && my < position.y + height;
	}


	@Override
	public boolean leftClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	@Override
	public boolean rightClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	/**
	 * The growth stage of a {@link Plant}
	 *
	 * @author Matt
	 */
	public enum GrowthStage {
		PLANTED, GERMINATED, UNRIPE, RIPE, WILTING, DEAD
	}
}