package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.IndividualIdentifier;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

/**
 * {@link CompositeAITask} consisting of:
 * 
 * Going to the location of a {@link Container}
 * Locking/Unlocking it.
 *
 * @author Matt
 */
public class LockUnlockContainer extends CompositeAITask {
	private static final long serialVersionUID = -1797728018857618555L;
	private Container container;
	private boolean lock;

	public LockUnlockContainer(Individual host, Prop container, boolean lock) {
		super(
			host.getId(),
			"Locking/Unlocking container",
			new GoToLocation(
				host,
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(container.position, 10, Domain.getWorld(host.getWorldId())), Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);
		
		appendTask(this.new LockUnlock(host.getId()));
		
		if (container instanceof Container) {
			this.lock = lock;
			this.container = (Container) container;
		} else {
			throw new RuntimeException("Can't lock/unlock something that is not a container");
		}
	}
	
	
	/**
	 * Lock/Unlock a {@link Container}
	 *
	 * @author Matt
	 */
	public class LockUnlock extends AITask {
		private static final long serialVersionUID = -2169557488767963869L;
		boolean complete = false;

		/**
		 * Constructor
		 */
		public LockUnlock(IndividualIdentifier id) {
			super(id);
		}

		@Override
		public String getDescription() {
			return "Locking/Unlocking";
		}

		@Override
		public boolean isComplete() {
			return complete;
		}

		@Override
		public void uponCompletion() {
		}

		@Override
		public void execute(float delta) {
			Domain.getIndividuals().get(hostId.getId()).getInventory().keySet().stream().forEach(item -> {
				if (lock) {
					if (container.lock(item)) {
						return;
					}
				} else {
					if (container.unlock(item)) {
						return;
					}
				}
			});
			complete = true;
		}
	}
}