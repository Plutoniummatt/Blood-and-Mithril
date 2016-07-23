package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.lockunlockcontainer.LockUnlockContainer;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * {@link Request} to instruct an {@link Individual} to {@link LockUnlockContainer}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class LockUnlockContainerRequest implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4618215967321196264L;
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
		Prop container = Domain.getWorld(Domain.getIndividual(individualId).getWorldId()).props().getProp(containerId);
		if (!(container instanceof Container)) {
			throw new RuntimeException("Can not lock/unlock non-container");
		}

		Individual individual = Domain.getIndividual(individualId);
		try {
			individual.getAI().setCurrentTask(
				new LockUnlockContainer(individual, container, lock)
			);
		} catch (NoTileFoundException e) {}

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