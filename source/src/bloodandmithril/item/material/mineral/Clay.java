package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.brick.Brick;

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
	public String getSingular(boolean firstCap) {
		return (firstCap ? "C" : "c") + "lay";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "C" : "c") + "lay";
	}


	@Override
	public String getDescription() {
		return "Clay";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Clay;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return heatLevel > 500 ? new Brick() : this;
	}


	@Override
	public void render() {
	}
}