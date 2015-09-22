package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

/**
 * A {@link CompositeAITask} that instructs the host to go to a location and plant a {@link SeedProp}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Plant seed")
public class PlantSeed extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -3292272671119971752L;

	private Vector2 targetLocation;
	private final SeedProp toPlant;

	@Inject
	PlantSeed() {
		super(null, "");
		this.toPlant = null;
	}

	/**
	 * Constructor
	 */
	public PlantSeed(Individual host, SeedProp toPlant) throws NoTileFoundException {
		super(
			host.getId(),
			"Planting seed",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(getGroundAboveOrBelowClosestEmptyOrPlatformSpace(toPlant.position, 10, Domain.getWorld(host.getWorldId())), Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		this.targetLocation = getGroundAboveOrBelowClosestEmptyOrPlatformSpace(toPlant.position, 10, Domain.getWorld(host.getWorldId()));
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
		public String getShortDescription() {
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


	@Override
	public String getDetailedDescription() {
		return getHost().getId().getSimpleName() + " plants " + toPlant.getTitle() + " at " + targetLocation.toString();
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine<? extends Visible> routine) {
		return null;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return null;
	}
}