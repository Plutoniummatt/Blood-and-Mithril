package bloodandmithril.character.ai.implementations;

import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public class HareAI extends ArtificialIntelligence {
	private static final long serialVersionUID = -4238810477533050722L;


	/**
	 * Constructor
	 */
	public HareAI(Individual host) {
		super(host);
	}


	@Override
	protected void determineCurrentTask() {
		wander(600f, false);
	}


	@Override
	protected ArtificialIntelligence internalCopy() {
		return new HareAI(getHost());
	}
}