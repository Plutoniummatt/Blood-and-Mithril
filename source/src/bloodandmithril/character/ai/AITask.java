package bloodandmithril.character.ai;

import java.io.Serializable;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Task;
import bloodandmithril.world.Domain;

/**
 * Abstract {@link Task}, see {@link ArtificialIntelligence#ArtificialIntelligence(bloodandmithril.character.individuals.Individual)}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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

	public IndividualIdentifier getHostId() {
		return hostId;
	}

	protected Individual getHost() {
		return Domain.getIndividual(hostId.getId());
	}

	/** @return the description of the task */
	public abstract String getShortDescription();

	/** @return the detailed description of the task */
	public abstract String getDetailedDescription();

	/** @return whether or not this task has been completed */
	public abstract boolean isComplete();

	/** Called upon task completion */
	public abstract boolean uponCompletion();

	/** Execute the implementation of this task. */
	public abstract void execute(float delta);
}
