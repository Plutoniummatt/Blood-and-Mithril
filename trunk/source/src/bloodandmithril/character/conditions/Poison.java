package bloodandmithril.character.conditions;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.Condition;

public class Poison implements Condition {

	/** The health/second being drained */
	public float toxicity;

	/** The decline in toxicity, per second */
	private final float persistence;

	public Poison(float toxicity, float persistence) {
		this.toxicity = toxicity;
		this.persistence = persistence;
	}

	@Override
	public void affect(Individual affected , float delta) {
		affected.state.health = affected.state.health - delta * toxicity;
		toxicity = toxicity - persistence * delta <= 0 ? 0 : toxicity - persistence * delta;
	}


	@Override
	public void infect(Individual infected, float delta) {
		// Non-infectious
	}


	@Override
	public boolean isExpired() {
		return toxicity <= 0;
	}
}
