package bloodandmithril.item.items.mineral.earth;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Just a pile of dirt
 *
 * @author Matt
 */
public class DirtItem extends EarthItem {
	private static final long serialVersionUID = 6522655675894787083L;
	
	/** {@link TextureRegion} of the {@link DirtItem} */
	public static TextureRegion DIRT_PILE;

	/**
	 * Constructor
	 */
	public DirtItem() {
		super(1f, 1, false, ItemValues.DIRT);
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
	public boolean rotates() {
		return false;
	}
	
	
	@Override
	public String getDescription() {
		return "Just a pile of dirt";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof DirtItem;
	}
	
	
	@Override
	public boolean throwable() {
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return DIRT_PILE;
	}


	@Override
	protected Item internalCopy() {
		return new DirtItem();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return DIRT_PILE;
	}
}