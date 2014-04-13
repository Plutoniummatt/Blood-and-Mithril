package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.item.Item;

/**
 * Lump of Steel
 *
 * @author Matt
 */
public class SteelIngot extends Item {
	private static final long serialVersionUID = -5395254759014196508L;

	/**
	 * Constructor
	 */
	public SteelIngot() {
		super(1f, false, 20);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "S" : "s") + "teel ingot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "S" : "s") + "teel ingots";
	}


	@Override
	public String getDescription() {
		return "An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from Steel.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof SteelIngot;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {
	}
}