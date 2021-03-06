package bloodandmithril.item.affix.postaffix;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.affix.Affix;
import bloodandmithril.item.affix.PostAffix;

/**
 * Improves health regeneration
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class HealthRegeneration extends PostAffix {
	private static final long serialVersionUID = -2608188062956678506L;

	private final float extraHealthRegen;

	/**
	 * Constructor
	 */
	public HealthRegeneration(float extraHealthRegen) {
		super();
		this.extraHealthRegen = extraHealthRegen;
	}


	@Override
	protected String getPostAffixDescription() {
		return "Health Regeneration";
	}


	@Override
	public boolean isSameAs(Affix other) {
		return other instanceof HealthRegeneration && extraHealthRegen == ((HealthRegeneration)other).extraHealthRegen;
	}
}