package bloodandmithril.character.ai;

import java.io.Serializable;

import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.util.Task;


/**
 * Abstract {@link Task}, see {@link ArtificialIntelligence#ArtificialIntelligence(bloodandmithril.character.Individual)}
 *
 * @author Matt
 */
public abstract class AITask implements Serializable {
	private static final long serialVersionUID = -3599103577896385879L;
	
	/** The host of this task */
	protected final IndividualIdentifier hostId;

	/**
	 * Protected constructor
	 */
	protected AITask(IndividualIdentifier hostId) {
		this.hostId = hostId;
	}

	/** @return the description of the task */
	public abstract String getDescription();

	/** @return whether or not this task has been completed */
	public abstract boolean isComplete();

	/** Called upon task completion */
	public abstract void uponCompletion();

	/** Execute the implementation of this task. */
	public abstract void execute();
}
