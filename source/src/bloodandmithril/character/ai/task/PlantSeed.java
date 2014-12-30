package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

/**
 * A {@link CompositeAITask} that instructs the host to go to a location and plant a {@link SeedProp}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PlantSeed extends CompositeAITask {
	private static final long serialVersionUID = -3292272671119971752L;

	private final SeedProp toPlant;

	/**
	 * Constructor
	 */
	public PlantSeed(Individual host, SeedProp toPlant) {
		super(
			host.getId(),
			"Planting seed",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(toPlant.position, 10, Domain.getWorld(host.getWorldId())), Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		this.toPlant = toPlant;

		appendTask(new Plant(hostId));
	}


	/**
	 * The task representing the actual planting of the seed
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2014")
	public class Plant extends AITask {
		private static final long serialVersionUID = -5888097320153824059L;

		boolean planted;

		public Plant(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "Planting seed";
		}


		@Override
		public boolean isComplete() {
			return planted;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			planted = true;
			Domain.getWorld(Domain.getIndividual(hostId.getId()).getWorldId()).props().addProp(toPlant);
			Domain.getIndividual(hostId.getId()).takeItem(toPlant.getSeed());
			UserInterface.refreshRefreshableWindows();
		}
	}
}