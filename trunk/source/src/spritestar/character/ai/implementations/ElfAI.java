package spritestar.character.ai.implementations;

import static spritestar.util.Util.*;

import spritestar.character.Individual;
import spritestar.character.ai.AITask;
import spritestar.character.ai.ArtificialIntelligence;
import spritestar.character.ai.task.Idle;

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
		wander(600f, false);
	}
	
	
	@Override
	public synchronized AITask getCurrentTask() {
		return firstNonNull(currentTask, new Idle());
	}
}