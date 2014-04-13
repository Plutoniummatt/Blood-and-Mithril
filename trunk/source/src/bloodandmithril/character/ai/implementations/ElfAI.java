package bloodandmithril.character.ai.implementations;

import static bloodandmithril.util.Util.firstNonNull;
import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ArtificialIntelligence;
import bloodandmithril.character.ai.task.Idle;


/**
 * AI for elves
 *
 * @author Matt
 */
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
//		wander(600f, false);
	}


	@Override
	public synchronized AITask getCurrentTask() {
		return firstNonNull(currentTask, new Idle());
	}
}