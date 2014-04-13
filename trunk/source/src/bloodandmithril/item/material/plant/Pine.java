package bloodandmithril.item.material.plant;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.mineral.Ashes;

public class Pine extends Item {
	private static final long serialVersionUID = 1882318163053390592L;

	/**
	 * Constructor
	 */
	public Pine() {
		super(1f, false, ItemValues.PINE);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "P" : "p") + "ine";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "P" : "p") + "ine";
	}


	@Override
	public String getDescription() {
		return "Logs of pine trees.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Pine;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return new Ashes();
	}


	@Override
	public void render() {
	}
}