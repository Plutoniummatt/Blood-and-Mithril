package bloodandmithril.character.conditions;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2014")
@Name(name = "Exhaustion")
public class Exhaustion extends Condition {
	private static final long serialVersionUID = 1486454742433474423L;

	private final int affected;

	/**
	 * Constructor
	 */
	public Exhaustion(final int affected) {
		this.affected = affected;
	}


	@Override
	public void affect(final Individual affected, final float delta) {
		Wiring.injector().getInstance(IndividualStateService.class).decreaseHunger(affected, (1 - affected.getState().stamina) * 0.00001f);
	}


	@Override
	public void infect(final Individual infected, final float delta) {
		// Not infectious
	}


	@Override
	public boolean isExpired() {
		return Domain.getIndividual(affected).getState().stamina > 0.75f;
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public void stack(final Condition condition) {
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Fuckin tired mate";
	}


	@Override
	public String getName() {
		return getName(Domain.getIndividual(affected).getState().stamina);
	}


	/**
	 * See {@link Condition#getName()}
	 */
	public static String getName(final float stamina) {
		final int h = Math.round(stamina * 10f);
		switch (h) {
			case 0: return "Exhausted";
			case 1: return "Very tired";
			case 2: return "Very tired";
			case 3: return "Tired";
			case 4: return "Tired";
			case 5: return "Slightly tired";
			case 6: return "Slightly tired";
			case 7: return "Rapid breathing";
			case 8: return "Rapid breathing";
			case 9: return "Energetic";
			case 10: return "Energetic";
			default: throw new RuntimeException("Unexpected stamina level");
		}
	}


	@Override
	public void clientSideEffects(final Individual affected, final float delta) {
	}
}