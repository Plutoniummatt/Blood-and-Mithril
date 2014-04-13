package bloodandmithril.item.material.plant;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

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
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}

	@Override
	public void render() {

	}
}