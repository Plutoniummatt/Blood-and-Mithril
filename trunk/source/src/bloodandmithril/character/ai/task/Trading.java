package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;

/**
 * An {@link AITask} which indicates an {@link Individual} is trading
 *
 * @author Matt
 */
public class Trading extends AITask {
	private static final long serialVersionUID = 6325569855563214762L;

	/**
	 * Constructor
	 */
	public Trading(IndividualIdentifier hostId) {
		super(hostId);
	}


	@Override
	public String getDescription() {
		return "Trading";
	}


	@Override
	public boolean isComplete() {
		return false;
	}


	@Override
	public void uponCompletion() {
	}


	@Override
	public void execute() {
		// Do nothing
	}
}