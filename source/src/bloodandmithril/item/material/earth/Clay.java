package bloodandmithril.item.material.earth;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Clay
 *
 * @author Matt
 */
public class Clay extends Item {
	private static final long serialVersionUID = 883456114549112166L;

	/**
	 * Constructor
	 */
	public Clay() {
		super(0.5f, false, ItemValues.CLAY);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "C" : "c") + "lay";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "C" : "c") + "lay";
	}


	@Override
	public String getDescription() {
		return "Clay";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Clay;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Clay();
	}
}