package spritestar.prop.building;

import spritestar.Fortress;
import spritestar.prop.Prop;

/**
 * A building
 *
 * @author Matt
 */
public abstract class Building extends Prop {

	/** Dimensions of this {@link Building} */
	protected final int width, height;

	/**
	 * Constructor
	 */
	protected Building(float x, float y, int width, int height, boolean grounded) {
		super(x, y, grounded);
		this.width = width;
		this.height = height;
	}


	@Override
	public boolean isMouseOver() {
		float mx = Fortress.getMouseWorldX();
		float my = Fortress.getMouseWorldY();

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