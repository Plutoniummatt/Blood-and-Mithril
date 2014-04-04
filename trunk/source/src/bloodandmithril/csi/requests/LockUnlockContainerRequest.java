package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.LockUnlockContainer;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * {@link Request} to instruct an {@link Individual} to {@link LockUnlockContainer}
 *
 * @author Matt
 */
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
		Prop container = Domain.getProps().get(containerId);
		if (!(container instanceof Container)) {
			throw new RuntimeException("Can not lock/unlock non-container");
		}
		
		Individual individual = Domain.getIndividuals().get(individualId);
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