package bloodandmithril.character.conditions;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.Condition;

/**
 * {@link Condition} representing thirst
 *
 * @author Matt
 */
public class Thirst extends Condition {
	private static final long serialVersionUID = -3232484824763914755L;
	
	private final Individual affected;
	private final float oldStaminaRegen;

	/**
	 * Constructor
	 */
	public Thirst(Individual affected) {
		this.affected = affected;
		this.oldStaminaRegen = affected.state.staminaRegen;
		affected.changeStaminaRegen(oldStaminaRegen * 0.5f);
	}


	@Override
	public void affect(Individual affected, float delta) {
		if (affected.state.thirst == 0f) {
			affected.damage(delta * 0.03f);
		}
	}


	@Override
	public void infect(Individual infected, float delta) {
		// Not infectious
	}


	@Override
	public boolean isExpired() {
		return affected.state.thirst > 0.75f;
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Thirst is the craving for fluids, resulting in the basic instinct of animals to drink.  Has a detrimental effect on the regeneration of stamina, prolonged thirst will cause vital signs to weaken.";
	}


	@Override
	public String getName() {
		int h = Math.round(affected.state.thirst * 10f);
		switch (h) {
			case 0: return "Dying of thirst";
			case 1: return "Extremely thirsty";
			case 2: return "Extremely thirsty";
			case 3: return "Very thirsty";
			case 4: return "Very Thirsty";
			case 5: return "Thirsty";
			case 6: return "Thirsty";
			case 7: return "Dry mouth";
			case 8: return "Dry mouth";
			default: throw new RuntimeException("Unexpected thirst level");
		}
	}


	@Override
	public void uponExpiry() {
		affected.changeStaminaRegen(oldStaminaRegen);
	}
}