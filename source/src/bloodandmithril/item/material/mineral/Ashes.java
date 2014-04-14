package bloodandmithril.item.material.mineral;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

public class Ashes extends Item {
	private static final long serialVersionUID = 988154990456038686L;
	public static final String description = "The residue of combustion, mostly consisting of metal oxides.";

	/**
	 * Constructor
	 */
	public Ashes() {
		super(0.2f, false, ItemValues.ASHES);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return "Ashes";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return "Ashes";
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Ashes;
	}


	@Override
	public void render() {

	}
}