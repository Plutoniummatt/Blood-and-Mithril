package bloodandmithril.item.material.earth;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Just a pile of dirt
 *
 * @author Matt
 */
public class Dirt extends Item {
	private static final long serialVersionUID = 6522655675894787083L;

	/**
	 * Constructor
	 */
	public Dirt() {
		super(0.5f, false, ItemValues.DIRT);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "D" : "d") + "irt";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "D" : "d") + "irt";
	}


	@Override
	public String getDescription() {
		return "Just a pile of dirt";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Dirt;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Dirt();
	}
}