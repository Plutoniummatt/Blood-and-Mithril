package bloodandmithril.character.ai.task.plantseed;

import static bloodandmithril.character.ai.pathfinding.PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace;
import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.food.plant.SeedItem;
import bloodandmithril.prop.plant.seed.SeedProp;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A {@link CompositeAITask} that instructs the host to go to a location and plant a {@link SeedProp}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@Name(name = "Plant seed")
@ExecutedBy(PlantSeedExecutor.class)
@RoutineContextMenusProvidedBy(PlantSeedRoutineContextMenuProvider.class)
public class PlantSeed extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -3292272671119971752L;

	final SeedProp toPlant;

	@Inject
	@Deprecated
	PlantSeed() {
		super(null, "");
		this.toPlant = null;
	}

	/**
	 * Constructor
	 */
	public PlantSeed(final Individual host, final SeedProp toPlant) throws NoTileFoundException {
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

		this.toPlant = toPlant;

		appendTask(new Plant(hostId));
	}


	/**
	 * The task representing the actual planting of the seed
	 *
	 * @author Matt
	 */
	@Copyright("Matthew Peck 2014")
	@ExecutedBy(PlantExecutor.class)
	public class Plant extends AITask {
		private static final long serialVersionUID = -5888097320153824059L;

		boolean planted;

		public Plant(final IndividualIdentifier hostId) {
			super(hostId);
		}
		
		
		PlantSeed getParent() {
			return PlantSeed.this;
		}


		@Override
		public String getShortDescription() {
			return "Planting seed";
		}
	}


	public static class PlantSeedTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 8576792777955769423L;
		private List<Vector2> locations;
		private int hostId;
		private SeedItem item;
		public PlantSeedTaskGenerator(final List<Vector2> locations, final int hostId, final SeedItem item) {
			this.locations = locations;
			this.hostId = hostId;
			this.item = item;
		}
		@Override
		public AITask apply(final Object input) {
			try {
				PlantSeed plantSeed = null;
				for (final Vector2 location : locations) {
					final SeedProp propSeed = item.getPropSeed();
					propSeed.position = location.cpy();

					if (plantSeed == null) {
						plantSeed = new PlantSeed(Domain.getIndividual(hostId), propSeed);
					} else {
						plantSeed.appendTask(new PlantSeed(Domain.getIndividual(hostId), propSeed));
					}
				}

				return plantSeed;
			} catch (final Exception e) {
				return null;
			}
		}
		@Override
		public String getDailyRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}
		private String getDescription() {
			if (locations.size() == 1) {
				final Vector2 location = locations.get(0);
				return "Plant " + item.getSingular(false) + " at " + String.format("%.1f", location.x) + ", " + String.format("%.1f", location.y);
			} else {
				return "Plant " + item.getSingular(false) + " at multiple locations";
			}
		}
		@Override
		public boolean valid() {
			if (Domain.getIndividual(hostId).has(item) == 0) {
				return false;
			}

			try {
				for (final Vector2 location : locations) {
					final SeedProp propSeed = item.getPropSeed();
					propSeed.position = location.cpy();
					new PlantSeed(Domain.getIndividual(hostId), propSeed);
				}
				return true;
			} catch (final Exception e) {
				return false;
			}
		}
		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			final Individual attacker = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			userInterface.getShapeRenderer().setColor(Color.RED);
			for (final Vector2 location : locations) {
				userInterface.getShapeRenderer().circle(worldToScreenX(location.x), worldToScreenY(location.y), 3f);
			}
			userInterface.getShapeRenderer().end();
		}
	}
}