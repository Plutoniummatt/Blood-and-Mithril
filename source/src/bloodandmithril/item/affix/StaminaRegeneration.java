package bloodandmithril.item.affix;

/**
 * Improves health regeneration
 *
 * @author Matt
 */
public class StaminaRegeneration extends PostAffix {
	private static final long serialVersionUID = -2608188062956678506L;

	private float extraStaminaRegen;

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