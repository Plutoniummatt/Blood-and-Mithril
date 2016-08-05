package bloodandmithril.character.ai.task.harvest;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.RoutineContextMenusProvidedBy;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.routine.entityvisible.EntityVisibleRoutine.VisiblePropFuture;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.prop.Harvestable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Harvest a {@link Harvestable}
 *
 * {@link GoToLocation} of the tile.
 *
 * @author Matt
 */
@Name(name = "Harvest")
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
@RoutineContextMenusProvidedBy(HarvestRoutineContextMenuProvider.class)
public final class Harvest extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -4098455998844182430L;

	/** Coordinate of the {@link Harvestable} to harvest */
	final Prop harvestable;

	@Inject
	@Deprecated
	Harvest() {
		super(null, "");
		this.harvestable = null;
	}

	/**
	 * Constructor
	 *
	 * @param coordinate - World pixel coordinate of the {@link Harvestable} to harvest.
	 */
	public Harvest(final Individual host, final Harvestable harvestable) throws NoTileFoundException {
		super(
			host.getId(),
			"Harvesting",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(((Prop) harvestable).position, 10, Domain.getWorld(host.getWorldId())).get(), 3 * Topography.TILE_SIZE),
				false,
				50f,
				true
			)
		);

		appendTask(this.new HarvestItem(host.getId()));

		this.harvestable = (Prop) harvestable;
	}


	/**
	 * Task of mining a tile
	 *
	 * @author Matt
	 */
	@ExecutedBy(HarvestItemExecutor.class)
	public final class HarvestItem extends AITask {
		private static final long serialVersionUID = 7585777004625914828L;
		boolean taskDone = false;

		/**
		 * Constructor
		 */
		public HarvestItem(final IndividualIdentifier hostId) {
			super(hostId);
		}
		
		
		public Harvest getParent() {
			return Harvest.this;
		}


		@Override
		public final String getShortDescription() {
			return "Harveseting";
		}
	}


	public static final class HarvestSelectedHarvestablesTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -501687183987861906L;
		private final int hostId;
		private final List<Integer> harvestableIds;
		private final int worldId;

		public HarvestSelectedHarvestablesTaskGenerator(final int hostId, final List<Integer> harvestableIds, final int worldId) {
			this.hostId = hostId;
			this.harvestableIds = harvestableIds;
			this.worldId = worldId;
		}

		@Override
		public AITask apply(final Object input) {
			if (valid()) {
				try {
					final List<Harvestable> validEntities = Lists.newLinkedList();
					for (final int i : harvestableIds) {
						if (Domain.getWorld(worldId).props().hasProp(i)) {
							final Prop prop = Domain.getWorld(worldId).props().getProp(i);
							if (Harvestable.class.isAssignableFrom(prop.getClass())) {
								validEntities.add((Harvestable) prop);
							}
						}
					}

					if (validEntities.isEmpty()) {
						return null;
					} else if (validEntities.size() == 1) {
						return new Harvest(getIndividual(hostId), validEntities.get(0));
					} else {
						final Harvest harvest = new Harvest(getIndividual(hostId), validEntities.get(0));
						validEntities.remove(0);
						for (final Harvestable h : validEntities) {
							harvest.appendTask(new Harvest(getIndividual(hostId), h));
						}

						return harvest;
					}
				} catch (final NoTileFoundException e) {
					return null;
				}
			}
			return null;
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

		@Override
		public boolean valid() {
			for (final int i : harvestableIds) {
				if (getWorld(worldId).props().hasProp(i)) {
					if (Harvestable.class.isAssignableFrom(getWorld(worldId).props().getProp(i).getClass())) {
						return true;
					}
				}
			}

			return false;
		}

		private String getDescription() {
			if (valid()) {
				return "Harvest selected entities";
			}
			return "";
		}

		@Override
		public void render() {
			final List<Prop> validEntities = Lists.newLinkedList();
			for (final int i : harvestableIds) {
				if (Domain.getWorld(worldId).props().hasProp(i)) {
					final Prop prop = Domain.getWorld(worldId).props().getProp(i);
					if (Harvestable.class.isAssignableFrom(prop.getClass())) {
						validEntities.add(prop);
					}
				}
			}
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);

			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual harvestable = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(harvestable.getState().position.x) - harvestable.getWidth()/2,
				worldToScreenY(harvestable.getState().position.y),
				harvestable.getWidth(),
				harvestable.getHeight()
			);
			userInterface.getShapeRenderer().setColor(Color.RED);
			Gdx.gl20.glLineWidth(2f);
			for (final Prop p : validEntities) {
				userInterface.getShapeRenderer().rect(
					worldToScreenX(p.position.x) - p.width/2,
					worldToScreenY(p.position.y),
					p.width,
					p.height
				);

			}
			userInterface.getShapeRenderer().end();
		}
	}


	public static final class HarvestAreaTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = 7331795787474572204L;

		private float left, right, top, bottom;
		private int hostId;

		/**
		 * Constructor
		 */
		public HarvestAreaTaskGenerator(final Vector2 start, final Vector2 finish, final int hostId) {
			this.hostId = hostId;
			this.left 	= min(start.x, finish.x);
			this.right 	= max(start.x, finish.x);
			this.top 	= max(start.y, finish.y);
			this.bottom	= min(start.y, finish.y);
		}

		@Override
		public final AITask apply(final Object input) {
			final Individual individual = getIndividual(hostId);
			final World world = getWorld(individual.getWorldId());
			final List<Integer> propsWithinBounds = world.getPositionalIndexMap().getEntitiesWithinBounds(Prop.class, left, right, top, bottom);

			final Wrapper<Harvest> task = new Wrapper<>(null);

			propsWithinBounds
			.stream()
			.filter(id -> {
				return Harvestable.class.isAssignableFrom(world.props().getProp(id).getClass());
			})
			.map(id -> {
				return (Harvestable) world.props().getProp(id);
			})
			.forEach(harvestable -> {
				try {
					if (task.t == null) {
						task.t = new Harvest(individual, harvestable);
					} else {
						task.t.appendTask(new Harvest(individual, harvestable));
					}
				} catch (final Exception e) {}
			});

			return task.t;
		}
		@Override
		public final String getDailyRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public final String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public final String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}
		@Override
		public final String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}
		private String getDescription() {
			return "Harvest from a defined area";
		}
		@Override
		public boolean valid() {
			return true;
		}
		@Override
		public void render() {
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			final Individual harvester = Domain.getIndividual(hostId);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(harvester.getState().position.x) - harvester.getWidth()/2,
				worldToScreenY(harvester.getState().position.y),
				harvester.getWidth(),
				harvester.getHeight()
			);

			userInterface.getShapeRenderer().setColor(Color.RED);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(left),
				worldToScreenY(bottom),
				right - left,
				top - bottom
			);

			userInterface.getShapeRenderer().end();
		}
	}


	public static final class HarvestVisibleEntityTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -2298234351386598398L;
		private final VisiblePropFuture propId;
		private final int hostId, worldId;

		public HarvestVisibleEntityTaskGenerator(final int hostId, final int worldId, final VisiblePropFuture propId) {
			this.hostId = hostId;
			this.worldId = worldId;
			this.propId = propId;
		}

		@Override
		public final AITask apply(final Object input) {
			final Prop prop = Domain.getWorld(worldId).props().getProp(propId.call());
			if (Harvestable.class.isAssignableFrom(prop.getClass())) {
				try {
					return new Harvest(Domain.getIndividual(hostId), (Harvestable) prop);
				} catch (final NoTileFoundException e) {
					return null;
				}
			}

			return null;
		}

		@Override
		public final String getDailyRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getEntityVisibleRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getIndividualConditionRoutineDetailedDescription() {
			return getDescription();
		}

		@Override
		public final String getStimulusDrivenRoutineDetailedDescription() {
			return getDescription();
		}

		private String getDescription() {
			return Domain.getIndividual(hostId).getId().getSimpleName() + " harvests any visible harvestable entity";
		}

		@Override
		public final boolean valid() {
			return true;
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

			userInterface.getShapeRenderer().end();
		}
	}
}