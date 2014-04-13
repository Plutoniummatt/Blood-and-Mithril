package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.material.metal.IronIngot;

/**
 * Otherwise known as Iron ore.
 *
 * @author Matt
 */
public class Hematite extends Item {
	private static final long serialVersionUID = 5544474463358187047L;

	/**
	 * Constructor
	 */
	public Hematite() {
		super(2f, false, 10);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "H" : "h") + "ematite";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "H" : "h") + "ematite";
	}


	@Override
	public String getDescription() {
		return "Hematite is a mineral, colored black to steel or silver-gray, brown to reddish brown, or red. It is mined as the main ore of iron.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Hematite;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return heatLevel >= 1400 ? new IronIngot() : this;
	}


	@Override
	public void render() {
	}
}