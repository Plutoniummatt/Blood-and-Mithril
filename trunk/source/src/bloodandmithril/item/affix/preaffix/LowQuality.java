package bloodandmithril.item.affix.preaffix;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.affix.Affix;
import bloodandmithril.item.affix.PreAffix;
import bloodandmithril.item.items.Item;

@Copyright("Matthew Peck 2014")
public class LowQuality extends PreAffix {
	private static final long serialVersionUID = 5750270806066218014L;
	private final float valueModifier;

	/**
	 * Constructor
	 */
	public LowQuality(float valueModifier) {
		this.valueModifier = valueModifier;
	}


	@Override
	protected String getPreAffixDescription() {
		return "Low Quality";
	}


	@Override
	public boolean isSameAs(Affix other) {
		return other instanceof LowQuality && ((LowQuality)other).valueModifier == valueModifier;
	}


	@Override
	public void itemEffects(Item item) {
	}


	@Override
	public void itemEffects(Individual individual, Item item) {
	}
}