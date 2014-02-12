package bloodandmithril.prop.building;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.GameWorld.Depth;

/**
 * A Construction
 *
 * @author Matt
 */
public abstract class Construction extends Prop {

	/** Dimensions of this {@link Construction} */
	protected final int width, height;

	/**
	 * Constructor
	 */
	protected Construction(float x, float y, int width, int height, boolean grounded) {
		super(x, y, grounded, Depth.MIDDLEGROUND);
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
}