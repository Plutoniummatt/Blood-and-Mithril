package bloodandmithril.character.ai.task.lightlightable;

import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
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
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.datastructure.Wrapper;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.WorldProps;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * Task that instructs the host to light a {@link Lightable}
 *
 * @author Matt
 */
@Name(name = "Light")
@Copyright("Matthew Peck 2015")
@ExecutedBy(CompositeAITaskExecutor.class)
@RoutineContextMenusProvidedBy(LightLightableRoutineContextMenuProvider.class)
public class LightLightable extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -2379179198784328909L;

	private boolean auto;
	int lightableId;

	@Inject
	LightLightable() {
		super(null, "");
	}

	/**
	 * Constructor
	 */
	public LightLightable(final Individual host, final Lightable lightable, final boolean auto) throws NoTileFoundException {
		super(
			host.getId(),
			"Lighting"
		);

		this.auto = auto;

		appendTask(
		GoToLocation.goToWithTerminationFunction(
			host,
			host.getState().position.cpy(),
			new WayPoint(PathFinder.getGroundAboveOrBelowClosestEmptyOrPlatformSpace(((Prop) lightable).position, 10, Domain.getWorld(host.getWorldId())), 0f),
			false,
			new WithinInteractionBox(),
			true
		));

		this.lightableId = ((Prop) lightable).id;
		appendTask(new LightFire(hostId));
	}


	public class WithinInteractionBox implements SerializableFunction<Boolean> {
		private static final long serialVersionUID = -6658375092168650175L;

		@Override
		public Boolean call() {
			final Lightable prop = (Lightable) Domain.getWorld(getHost().getWorldId()).props().getProp(lightableId);
			return prop == null || prop.isLit() && auto || getHost().getInteractionBox().isWithinBox(Domain.getWorld(getHost().getWorldId()).props().getProp(lightableId).position);
		}
	}


	/**
	 * Actual lighting of the fire
	 *
	 * @author Matt
	 */
	@ExecutedBy(LightFireExecutor.class)
	public class LightFire extends AITask {
		private static final long serialVersionUID = -5213896264414790155L;
		boolean lit;

		public LightFire(final IndividualIdentifier hostId) {
			super(hostId);
		}
		
		
		public LightLightable getParent() {
			return LightLightable.this;
		}


		@Override
		public String getShortDescription() {
			return null;
		}
	}


	public static final class LightSelectedLightablesTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -203687123987361534L;
		private final int hostId;
		private final List<Integer> lightableIds;
		private final int worldId;

		public LightSelectedLightablesTaskGenerator(final int hostId, final List<Integer> lightableIds, final int worldId) {
			this.hostId = hostId;
			this.lightableIds = lightableIds;
			this.worldId = worldId;
		}

		@Override
		public AITask apply(final Object input) {
			if (valid()) {
				try {
					final List<Integer> validEntities = Lists.newLinkedList();
					for (final int i : lightableIds) {
						final WorldProps props = Domain.getWorld(worldId).props();
						if (props.hasProp(i) && Lightable.class.isAssignableFrom(props.getProp(i).getClass())) {
							validEntities.add(i);
						}
					}

					final LightLightable lightLightable = new LightLightable(getIndividual(hostId), (Lightable) getWorld(worldId).props().getProp(validEntities.get(0)), true);
					final ArrayList<Integer> lightableIdsCopy = Lists.newArrayList(validEntities);
					lightableIdsCopy.remove(0);
					for (final int id : lightableIdsCopy) {
						lightLightable.appendTask(new LightLightable(getIndividual(hostId), (Lightable) getWorld(worldId).props().getProp(id), true));
					}

					return lightLightable;
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
			for (final int id : lightableIds) {
				if (getWorld(worldId).props().hasProp(id)) {
					final Prop prop = getWorld(worldId).props().getProp(id);
					if (Lightable.class.isAssignableFrom(prop.getClass()) && ((Lightable) prop).canLight() && !((Lightable) prop).isLit()) {
						return true;
					}
				}
			}

			return false;
		}

		private String getDescription() {
			if (valid()) {
				return "Light selected lightables";
			}
			return "";
		}

		@Override
		public void render() {
			final List<Prop> validEntities = Lists.newLinkedList();
			for (final int i : lightableIds) {
				if (Domain.getWorld(worldId).props().hasProp(i)) {
					final Prop prop = Domain.getWorld(worldId).props().getProp(i);
					if (Lightable.class.isAssignableFrom(prop.getClass())) {
						validEntities.add(prop);
					}
				}
			}
			final UserInterface userInterface = Wiring.injector().getInstance(UserInterface.class);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
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


	public static class LightLightablesInAreaTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -4156962319483950808L;
		private final float left, right, top, bottom;
		private final int hostId;

		public LightLightablesInAreaTaskGenerator(final Vector2 start, final Vector2 finish, final int hostId) {
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

			final Wrapper<LightLightable> task = new Wrapper<LightLightable>(null);

			propsWithinBounds
			.stream()
			.filter(id -> {
				return Lightable.class.isAssignableFrom(world.props().getProp(id).getClass());
			})
			.map(id -> {
				return (Lightable) world.props().getProp(id);
			})
			.forEach(lightable -> {
				try {
					if (task.t == null) {
						task.t = new LightLightable(individual, lightable, true);
					} else {
						task.t.appendTask(new LightLightable(individual, lightable, true));
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
			return "Light lightables in a defined area";
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


	public static class GenerateLightAnyVisibleLightables extends TaskGenerator {
		private static final long serialVersionUID = 1898797069712115415L;
		private int individualId;

		public GenerateLightAnyVisibleLightables(final int individualId) {
			this.individualId = individualId;
		}

		@Override
		public AITask apply(final Object input) {
			if (!(input instanceof Lightable)) {
				return null;
			}

			try {
				final Individual individual = Domain.getIndividual(individualId);
				if (individual.getFireLighter() == null || !individual.isAlive()) {
					return null;
				}
				return new LightLightable(individual, (Lightable) input, true);
			} catch (final NoTileFoundException e) {
				return null;
			}
		}

		@Override
		public String getDailyRoutineDetailedDescription() {
			throw new UnsupportedOperationException("It does not make sense to generate generic light lightables task in a daily routine");
		}

		@Override
		public String getEntityVisibleRoutineDetailedDescription() {
			return Domain.getIndividual(individualId).getId().getSimpleName() + " lights visible lightable prop";
		}

		@Override
		public String getIndividualConditionRoutineDetailedDescription() {
			throw new UnsupportedOperationException("It does not make sense to generate generic light lightables task in a individual condition routine");
		}

		@Override
		public String getStimulusDrivenRoutineDetailedDescription() {
			throw new UnsupportedOperationException("It does not make sense to generate generic light lightables task in a stimulus driven routine");
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
			final Individual attacker = Domain.getIndividual(individualId);
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