package bloodandmithril.item.material.container;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.ui.components.window.Window;

/**
 * Bucket that can contain {@link Liquid}s
 *
 * @author Matt
 */
public class Bucket extends Item {
	private static final long serialVersionUID = -4162891941797527242L;

	/**
	 * Constructor
	 */
	protected Bucket(float mass, boolean equippable, long value) {
		super(mass, equippable, value);
	}

	@Override
	public String getSingular(boolean firstCap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPlural(boolean firstCap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Window getInfoWindow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean sameAs(Item other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Item combust(int heatLevel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

}
