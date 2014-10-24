package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.LockUnlockContainer;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to instruct an {@link Individual} to {@link LockUnlockContainer}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class LockUnlockContainerRequest implements Request {

	private int individualId;
	private int containerId;
	private boolean lock;

	/**
	 * Constructor
	 */
	public LockUnlockContainerRequest(int individualId, int containerId, boolean lock) {
		this.individualId = individualId;
		this.containerId = containerId;
		this.lock = lock;
	}


	@Override
	public Responses respond() {
		Prop container = Domain.getProp(containerId);
		if (!(container instanceof Container)) {
			throw new RuntimeException("Can not lock/unlock non-container");
		}

		Individual individual = Domain.getIndividual(individualId);
		individual.getAI().setCurrentTask(
			new LockUnlockContainer(individual, container, lock)
		);

		return new Responses(false);
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}