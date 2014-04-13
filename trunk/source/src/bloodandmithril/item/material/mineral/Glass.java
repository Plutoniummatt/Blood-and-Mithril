package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

public class Glass extends Item {
	private static final long serialVersionUID = -1491126318224334985L;

	/**
	 * Constructor
	 */
	public Glass() {
		super(0.5f, false, ItemValues.GLASS);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass";
	}


	@Override
	public String getDescription() {
		return "Silicate glass";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Glass;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {
	}
}