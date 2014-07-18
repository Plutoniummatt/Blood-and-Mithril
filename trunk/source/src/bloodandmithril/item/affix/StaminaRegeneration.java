package bloodandmithril.item.affix;

import bloodandmithril.core.Copyright;

/**
 * Improves health regeneration
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class StaminaRegeneration extends PostAffix {
	private static final long serialVersionUID = -2608188062956678506L;

	private final float extraStaminaRegen;

	/**
	 * Constructor
	 */
	public StaminaRegeneration(float extraStaminaRegen) {
		super();
		this.extraStaminaRegen = extraStaminaRegen;
	}


	@Override
	protected String getPostAffixDescription() {
		return "Stamina Regeneration";
	}


	@Override
	public boolean isSameAs(Affix other) {
		return other instanceof StaminaRegeneration && extraStaminaRegen == ((StaminaRegeneration)other).extraStaminaRegen;
	}
}