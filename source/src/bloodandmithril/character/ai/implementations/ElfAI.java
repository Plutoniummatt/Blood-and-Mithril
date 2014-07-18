package bloodandmithril.character.ai.implementations;

import static bloodandmithril.util.Util.firstNonNull;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * AI for elves
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ElfAI extends ArtificialIntelligence {
	private static final long serialVersionUID = -6956695432238102289L;


	/**
	 * Constructor
	 */
	public ElfAI(Individual host) {
		super(host);
	}


	@Override
	protected void determineCurrentTask() {
	}


	@Override
	public synchronized AITask getCurrentTask() {
		return firstNonNull(currentTask, new Idle());
	}
}