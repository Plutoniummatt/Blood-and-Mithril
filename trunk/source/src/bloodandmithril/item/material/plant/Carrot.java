package bloodandmithril.item.material.plant;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.mineral.Ashes;

/**
 * A Carrot
 *
 * @author Matt
 */
public class Carrot extends Item implements Consumable {
	private static final long serialVersionUID = 3714624810622084079L;
	public static final String description = "The carrot is a root vegetable, usually orange in color. It has a crisp texture when fresh.";

	/**
	 * Constructor
	 */
	public Carrot() {
		super(0.1f, false, ItemValues.CARROT);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Carrot" : "carrot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Carrots" : "carrots";
	}


	@Override
	public boolean consume(Individual consumer) {
		consumer.increaseHunger(0.05f);
		return true;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof Carrot) {
			return true;
		}
		return false;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		if (heatLevel > 200 && heatLevel < 500f) {
			return new CookedCarrot();
		} else {
			return new Ashes();
		}
	}


	@Override
	public void render() {

	}
}