package bloodandmithril.character.ai;

import java.io.Serializable;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
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

	private transient boolean injected = false;

	/**
	 * Protected constructor
	 */
	protected AITask(final IndividualIdentifier hostId) {
		this.hostId = hostId;

		// Sadface...
		Wiring.injector().injectMembers(this);
		this.injected = true;
	}

	public IndividualIdentifier getHostId() {
		return hostId;
	}

	protected Individual getHost() {
		return Domain.getIndividual(hostId.getId());
	}

	/** @return the description of the task */
	public abstract String getShortDescription();

	/** @return whether or not this task has been completed */
	public abstract boolean isComplete();

	/** Called upon task completion */
	public abstract boolean uponCompletion();

	/** Execute the implementation of this task. */
	public abstract void execute(float delta);


	public boolean isInjected() {
		return injected;
	}

	public void setInjected(final boolean injected) {
		this.injected = injected;
	}
}
