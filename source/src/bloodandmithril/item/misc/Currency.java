package bloodandmithril.item.misc;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

/**
 * Class representing currency
 *
 * @author Matt
 */
public class Currency extends Item {
	private static final long serialVersionUID = -7059495735666011863L;

	/**
	 * Constructor
	 */
	public Currency() {
		super(0f, false, ItemValues.CURRENCY);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Coin" : "coin";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Coins" : "coins";
	}


	@Override
	public String getDescription() {
		return "The most widely used form of currency, can be used for trade as a substitution for items.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Currency;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {

	}
}