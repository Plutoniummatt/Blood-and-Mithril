package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.equipment.misc.FlintAndFiresteel;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.math.Vector2;

/**
 * Task that instructs the host to light a {@link Campfire}
 *
 * @author Matt
 */
public class LightCampfire extends CompositeAITask {
	private static final long serialVersionUID = -2379179198784328909L;

	private int campfireId;

	/**
	 * Constructor
	 */
	public LightCampfire(Individual host, Campfire campfire) {
		super(
			host.getId(),
			"Mining",
			new GoToLocation(
				host,
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(campfire.position, 10, Domain.getWorld(host.getWorldId())), 3 * Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		this.campfireId = campfire.id;
		appendTask(new LightFire(hostId));
	}


	/**
	 * Actual lighting of the fire
	 *
	 * @author Matt
	 */
	public class LightFire extends AITask {
		private static final long serialVersionUID = -5213896264414790155L;
		private boolean lit;

		public LightFire(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return null;
		}


		@Override
		public boolean isComplete() {
			return Domain.hasProp(campfireId) && lit;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			Individual host = Domain.getIndividual(hostId.getId());

			if (!Domain.hasProp(campfireId)) {
				return;
			}

			Campfire campfire = (Campfire) Domain.getProp(campfireId);
			if (host.getInteractionBox().isWithinBox(campfire.position)) {
				if (host.has(new FlintAndFiresteel()) > 0) {
					ParticleService.parrySpark(campfire.position.cpy().add(0, 7), new Vector2());
					campfire.setLit(true);
					lit = true;
				}
			}
		}
	}
}