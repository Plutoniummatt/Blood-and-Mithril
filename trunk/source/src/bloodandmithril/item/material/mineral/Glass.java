package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Glass extends Item {
	private static final long serialVersionUID = -1491126318224334985L;

	/**
	 * Constructor
	 */
	public Glass() {
		super(0.5f, false, ItemValues.GLASS);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass";
	}


	@Override
	public String getDescription() {
		return "Silicate glass";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Glass;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Glass();
	}
}