package bloodandmithril.character.conditions;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.world.Domain;

/**
 * {@link Condition} representing Hunger
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Hunger")
public class Hunger extends Condition {
	private static final long serialVersionUID = 6876432751800675014L;

	private final int affected;

	/**
	 * Constructor
	 */
	public Hunger(final int affected) {
		this.affected = affected;
		final float healthRegen = Domain.getIndividual(affected).getState().healthRegen;
		Domain.getIndividual(affected).changeHealthRegen(healthRegen * 0.5f);
	}


	/**
	 * @see bloodandmithril.character.individuals.Individual.Condition#affect(bloodandmithril.character.individuals.Individual, float)
	 *
	 * If we're starving (hunger == 0) then individual takes damage, precisely 0.01 per second.
	 * Also health regen zeroes.
	 *
	 * If hunger is not zero, then restore health regen to old health regen
	 */
	@Override
	public void affect(final Individual affected, final float delta) {
		if (affected.getState().hunger == 0f) {
			Wiring.injector().getInstance(IndividualStateService.class).damage(affected, delta * 0.01f);
			if (affected.getState().healthRegen != 0f) {
				affected.changeHealthRegen(0f);
			}
		}
	}


	@Override
	public void infect(final Individual infected, final float delta) {
		// Not infectious
	}


	@Override
	public boolean isExpired() {
		return Domain.getIndividual(affected).getState().hunger > 0.75f;
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Hunger is the physical sensation of desiring food.  Has a detrimental effect on natural wound healing, prolonged hunger will cause vital signs to weaken.";
	}


	@Override
	public String getName() {
		return getName(Domain.getIndividual(affected).getState().hunger);
	}


	/**
	 * See {@link Condition#getName()}
	 */
	public static String getName(final float hunger) {
		final int h = Math.round(hunger * 10f);
		switch (h) {
			case 0: return "Starving";
			case 1: return "Ravenous";
			case 2: return "Very hungry";
			case 3: return "Very hungry";
			case 4: return "Hungry";
			case 5: return "Hungry";
			case 6: return "Hungry";
			case 7: return "Peckish";
			case 8: return "Peckish";
			case 9: return "Not hungry";
			case 10: return "Not hungry";
			default: throw new RuntimeException("Unexpected hunger level");
		}
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public void stack(final Condition condition) {
	}


	@Override
	public void clientSideEffects(final Individual affected, final float delta) {
	}
}