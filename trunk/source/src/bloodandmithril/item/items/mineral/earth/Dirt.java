package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Just a pile of dirt
 *
 * @author Matt
 */
public class Dirt extends Earth {
	private static final long serialVersionUID = 6522655675894787083L;
	
	/** {@link TextureRegion} of the {@link Dirt} */
	public static TextureRegion DIRT_PILE;

	/**
	 * Constructor
	 */
	public Dirt() {
		super(10f, 10, false, ItemValues.DIRT);
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
	public TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Dirt();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return DIRT_PILE;
	}
}