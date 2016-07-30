package bloodandmithril.character.conditions;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;

/**
 * Drains health
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Poison")
public class Poison extends Condition {
	private static final long serialVersionUID = 1741945471582031402L;

	/** The health/second being drained */
	public float toxicity;

	/** The decline in toxicity, per second */
	private float persistence;

	public Poison(final float toxicity, final float persistence) {
		this.toxicity = toxicity;
		this.persistence = persistence;
	}

	@Override
	public void affect(final Individual affected , final float delta) {
		Wiring.injector().getInstance(IndividualStateService.class).damage(affected, delta * toxicity);
		toxicity = toxicity - persistence * delta <= 0 ? 0 : toxicity - persistence * delta;
	}


	@Override
	public void infect(final Individual infected, final float delta) {
		// Non-infectious
	}


	@Override
	public boolean isExpired() {
		return toxicity <= 0;
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Poisoned, the toxic effects will be detrimental to the general well-being of this individual, and can lead to fatal consequences";
	}

	@Override
	public String getName() {
		String severity;

		final int sev = Math.round(toxicity * 100)/10;
		switch (sev) {
			case 0:		severity = "Mildly"; break;
			case 1:		severity = "Mildly"; break;
			case 2:		severity = "Mildly"; break;
			case 3:		severity = "Moderately"; break;
			case 4:		severity = "Moderately"; break;
			case 5:		severity = "Strongly"; break;
			case 6:		severity = "Strongly"; break;
			case 7:		severity = "Strongly"; break;
			case 8:		severity = "Acutely"; break;
			case 9:		severity = "Acutely"; break;
			case 10:	severity = "Acutely"; break;
			default: 	severity = "Extremely"; break;
		}

		return severity + " poisoned";
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public void stack(final Condition condition) {
		if (!(condition instanceof Poison)) {
			throw new RuntimeException("Cannot stack " + condition.getClass().getSimpleName() + " with Poison");
		}

		this.persistence = ((Poison)condition).persistence * ((Poison)condition).toxicity/this.toxicity + this.persistence;
		this.toxicity = ((Poison)condition).toxicity + this.persistence;
	}


	@Override
	public void clientSideEffects(final Individual affected, final float delta) {
	}
}