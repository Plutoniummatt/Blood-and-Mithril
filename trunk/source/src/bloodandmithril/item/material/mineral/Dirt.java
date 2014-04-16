package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

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
	public String getSingular(boolean firstCap) {
		return (firstCap ? "D" : "d") + "irt";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "D" : "d") + "irt";
	}


	@Override
	public String getDescription() {
		return "Just a pile of dirt";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Dirt;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected float getRenderAngle() {
		// TODO Auto-generated method stub
		return 0;
	}
}