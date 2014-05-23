package bloodandmithril.character.conditions;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Condition;

/**
 * Drains health
 *
 * @author Matt
 */
public class Bleeding extends Condition {
	private static final long serialVersionUID = 2191121600917403074L;

	/** The health/second being drained */
	private float severity;

	/** Constructor */
	public Bleeding(float severity) {
		this.severity = severity;
	}

	@Override
	public void affect(Individual affected, float delta) {
		affected.damage(delta * severity);
	}


	@Override
	public void infect(Individual infected, float delta) {
		// Not infectious
	}


	@Override
	public boolean isExpired() {
		return severity <= 0f;
	}


	@Override
	public void uponExpiry() {
	}


	@Override
	public boolean isNegative() {
		return true;
	}


	@Override
	public String getHelpText() {
		return "Bleeding, technically known as hemorrhaging, is the loss of blood escaping from the circulatory system.  Vital signs become weaker over time if bleeding is not stopped.";
	}


	@Override
	public String getName() {
		String severity;

		int sev = Math.round(this.severity * 100)/10;
		switch (sev) {
			case 0:		severity = "Slight"; break;
			case 1:		severity = "Slight"; break;
			case 2:		severity = "Mild"; break;
			case 3:		severity = "Mild"; break;
			case 4:		severity = "Moderate"; break;
			case 5:		severity = "Moderate"; break;
			case 6:		severity = "Badly"; break;
			case 7:		severity = "Badly"; break;
			case 8:		severity = "Heavy"; break;
			case 9:		severity = "Heavy"; break;
			case 10:	severity = "Extreme"; break;
			default: 	severity = "Extreme"; break;
		}

		return severity + " bleeding";
	}


	@Override
	public void stack(Condition condition) {
		if (!(condition instanceof Bleeding)) {
			throw new RuntimeException("Cannot stack " + condition.getClass().getSimpleName() + " with Bleeding");
		}

		this.severity = this.severity + ((Bleeding) condition).severity;
	}
}