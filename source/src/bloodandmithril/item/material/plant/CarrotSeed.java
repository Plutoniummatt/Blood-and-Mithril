package bloodandmithril.item.material.plant;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CarrotSeed extends Seed {
	private static final long serialVersionUID = -3918937697003306522L;

	/**
	 * Constructor
	 */
	protected CarrotSeed() {
		super(0.001f, false, ItemValues.CARROTSEED);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Carrot seed" : "carrot seed";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Carrot seeds" : "carrot seeds";
	}


	@Override
	public String getDescription() {
		return "Seed of a carrot";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof CarrotSeed;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new CarrotSeed();
	}
}