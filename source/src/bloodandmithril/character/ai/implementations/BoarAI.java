package bloodandmithril.character.ai.implementations;

import static bloodandmithril.util.Util.firstNonNull;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public class BoarAI extends ArtificialIntelligence {
	private static final long serialVersionUID = -4238810477533050722L;


	/**
	 * Constructor
	 */
	public BoarAI(Individual host) {
		super(host);
	}


	@Override
	protected void determineCurrentTask() {
		wander(600f, false);
	}


	@Override
	public synchronized AITask getCurrentTask() {
		return firstNonNull(currentTask, new Idle());
	}
}