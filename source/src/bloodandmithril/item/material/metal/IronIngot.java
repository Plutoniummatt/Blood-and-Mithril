package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.fuel.Coal;

import com.google.common.collect.Iterables;

/**
 * Lump of iron
 *
 * @author Matt
 */
public class IronIngot extends Item {
	private static final long serialVersionUID = 5784780777572238051L;

	/**
	 * Constructor
	 */
	public IronIngot() {
		super(1f, false, ItemValues.IRONINGOT);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "I" : "i") + "ron ingot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "I" : "i") + "ron ingots";
	}


	@Override
	public String getDescription() {
		return "An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from Iron.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof IronIngot;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		boolean hasCoal = Iterables.tryFind(with.entrySet(), entry -> {
			return entry.getKey() instanceof Coal;
		}).isPresent();

		if (heatLevel >= 1400 && hasCoal) {
			return new SteelIngot();
		}
		return this;
	}


	@Override
	public void render() {
	}
}