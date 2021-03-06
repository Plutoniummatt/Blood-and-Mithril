package bloodandmithril.character.ai.implementations;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public final class WolfAI extends ArtificialIntelligence {
	private static final long serialVersionUID = 755427038153448789L;

	/**
	 * Constructor
	 */
	public WolfAI(Individual host) {
		super(host);
	}


	@Override
	protected final ArtificialIntelligence internalCopy() {
		return new WolfAI(getHost());
	}


	@Override
	protected final void determineCurrentTask() {
		wander(500f, false);
	}


	@Override
	public final void addRoutines() {
	}
}