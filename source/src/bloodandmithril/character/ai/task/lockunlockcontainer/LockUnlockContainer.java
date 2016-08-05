package bloodandmithril.character.ai.task.lockunlockcontainer;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * {@link CompositeAITask} consisting of:
 *
 * Going to the location of a {@link Container}
 * Locking/Unlocking it.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
public class LockUnlockContainer extends CompositeAITask {
	private static final long serialVersionUID = -1797728018857618555L;
	Container container;
	boolean lock;

	/**
	 * Constructor
	 */
	public LockUnlockContainer(final Individual host, final Prop container, final boolean lock) throws NoTileFoundException {
		super(
			host.getId(),
			"Locking/Unlocking container",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(container.position, 10, Domain.getWorld(host.getWorldId())).get(), Topography.TILE_SIZE),
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
	@ExecutedBy(LockUnlockExecutor.class)
	public class LockUnlock extends AITask {
		private static final long serialVersionUID = -2169557488767963869L;
		boolean complete = false;

		/**
		 * Constructor
		 */
		public LockUnlock(final IndividualIdentifier id) {
			super(id);
		}
		
		
		public LockUnlockContainer getParent() {
			return LockUnlockContainer.this;
		}
		

		@Override
		public String getShortDescription() {
			return "Locking/Unlocking";
		}
	}
}