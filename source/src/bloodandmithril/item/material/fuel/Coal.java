package bloodandmithril.item.material.fuel;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.item.material.mineral.Ashes;

public class Coal extends Item implements Fuel {
	private static final long serialVersionUID = 6399640412435082388L;

	/**
	 * Constructor
	 */
	public Coal() {
		super(0.1f, false, ItemValues.COAL);
	}


	@Override
	public float getCombustionDuration() {
		return 20;
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Coal" : "coal";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Coal" : "coal";
	}


	@Override
	public String getDescription() {
		return "Coal is a combustible black or brownish-black sedimentary rock usually occurring in rock strata in layers or veins called coal beds or coal seams";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Coal;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return new Ashes();
	}


	@Override
	public float getEnergy() {
		return 5000f;
	}


	@Override
	public void render() {

	}
}