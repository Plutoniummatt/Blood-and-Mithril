package bloodandmithril.character.ai.task;

import static bloodandmithril.control.InputUtilities.getMouseScreenX;
import static bloodandmithril.control.InputUtilities.getMouseScreenY;
import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;
import static bloodandmithril.world.Domain.getIndividual;
import static bloodandmithril.world.Domain.getWorld;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.FireLighter;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.cursorboundtask.ChooseAreaCursorBoundTask;
import bloodandmithril.util.cursorboundtask.ChooseMultipleEntityCursorBoundTask;
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
@Copyright("Matthew Peck 2015")
@Name(name = "Light")
public class LightLightable extends CompositeAITask implements RoutineTask {
	private static final long serialVersionUID = -2379179198784328909L;

	private boolean auto;
	private int lightableId;

	@Inject
	LightLightable() {
		super(null, "");
	}

	/**
	 * Constructor
	 */
	public LightLightable(Individual host, Lightable lightable, boolean auto) throws NoTileFoundException {
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
			Lightable prop = (Lightable) Domain.getWorld(getHost().getWorldId()).props().getProp(lightableId);
			return prop == null || prop.isLit() && auto || getHost().getInteractionBox().isWithinBox(Domain.getWorld(getHost().getWorldId()).props().getProp(lightableId).position);
		}
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
		public String getShortDescription() {
			return null;
		}


		@Override
		public boolean isComplete() {
			Prop prop = Domain.getWorld(getHost().getWorldId()).props().getProp(lightableId);
			return prop != null && (lit || ((Lightable) prop).isLit()) || !((Lightable) prop).canLight();
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			Individual host = Domain.getIndividual(hostId.getId());

			if (!Domain.getWorld(host.getWorldId()).props().hasProp(lightableId)) {
				return;
			}

			Lightable lightable = (Lightable) Domain.getWorld(host.getWorldId()).props().getProp(lightableId);

			if (!lightable.canLight()) {
				lit = true;
				return;
			}

			if (host.getInteractionBox().isWithinBox(((Prop) lightable).position)) {
				FireLighter fireLighter = host.getFireLighter();
				if (fireLighter != null) {
					fireLighter.fireLightingEffect((Prop) lightable);
					lightable.light();
				} else {
					host.speak("I need fire lighting equipment", 2000);
				}
				lit = true;
			}
		}
	}


	public static final class LightSelectedLightablesTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -203687123987361534L;
		private final int hostId;
		private final List<Integer> lightableIds;
		private final int worldId;

		public LightSelectedLightablesTaskGenerator(int hostId, List<Integer> lightableIds, int worldId) {
			this.hostId = hostId;
			this.lightableIds = lightableIds;
			this.worldId = worldId;
		}

		@Override
		public AITask apply(Object input) {
			if (valid()) {
				try {
					List<Integer> validEntities = Lists.newLinkedList();
					for (int i : lightableIds) {
						WorldProps props = Domain.getWorld(worldId).props();
						if (props.hasProp(i) && Lightable.class.isAssignableFrom(props.getProp(i).getClass())) {
							validEntities.add(i);
						}
					}

					LightLightable lightLightable = new LightLightable(getIndividual(hostId), (Lightable) getWorld(worldId).props().getProp(validEntities.get(0)), true);
					ArrayList<Integer> lightableIdsCopy = Lists.newArrayList(validEntities);
					lightableIdsCopy.remove(0);
					for (int id : lightableIdsCopy) {
						lightLightable.appendTask(new LightLightable(getIndividual(hostId), (Lightable) getWorld(worldId).props().getProp(id), true));
					}

					return lightLightable;
				} catch (NoTileFoundException e) {
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
			for (int id : lightableIds) {
				if (getWorld(worldId).props().hasProp(id)) {
					Prop prop = getWorld(worldId).props().getProp(id);
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
			List<Prop> validEntities = Lists.newLinkedList();
			for (int i : lightableIds) {
				if (Domain.getWorld(worldId).props().hasProp(i)) {
					Prop prop = Domain.getWorld(worldId).props().getProp(i);
					if (Lightable.class.isAssignableFrom(prop.getClass())) {
						validEntities.add(prop);
					}
				}
			}

			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			Individual harvestable = Domain.getIndividual(hostId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(harvestable.getState().position.x) - harvestable.getWidth()/2,
				worldToScreenY(harvestable.getState().position.y),
				harvestable.getWidth(),
				harvestable.getHeight()
			);
			UserInterface.shapeRenderer.setColor(Color.RED);
			Gdx.gl20.glLineWidth(2f);
			for (Prop p : validEntities) {
				UserInterface.shapeRenderer.rect(
					worldToScreenX(p.position.x) - p.width/2,
					worldToScreenY(p.position.y),
					p.width,
					p.height
				);

			}
			UserInterface.shapeRenderer.end();
		}
	}


	public static class LightLightablesInAreaTaskGenerator extends TaskGenerator {
		private static final long serialVersionUID = -4156962319483950808L;
		private final float left, right, top, bottom;
		private final int hostId;

		public LightLightablesInAreaTaskGenerator(Vector2 start, Vector2 finish, int hostId) {
			this.hostId = hostId;
			this.left 	= min(start.x, finish.x);
			this.right 	= max(start.x, finish.x);
			this.top 	= max(start.y, finish.y);
			this.bottom	= min(start.y, finish.y);
		}

		@Override
		public final AITask apply(Object input) {
			final Individual individual = getIndividual(hostId);
			World world = getWorld(individual.getWorldId());
			List<Integer> propsWithinBounds = world.getPositionalIndexMap().getEntitiesWithinBounds(Prop.class, left, right, top, bottom);

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
				} catch (Exception e) {}
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
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			Gdx.gl20.glLineWidth(2f);
			Individual attacker = Domain.getIndividual(hostId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);

			UserInterface.shapeRenderer.setColor(Color.RED);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(left),
				worldToScreenY(bottom),
				right - left,
				top - bottom
			);

			UserInterface.shapeRenderer.end();
		}
	}


	private ContextMenu getContextMenu(Routine routine, Individual host) {
		return new ContextMenu(getMouseScreenX(), getMouseScreenY(), true,
			new MenuItem(
				"Light lightables in area",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseAreaCursorBoundTask(
							args -> {
								routine.setAiTaskGenerator(new LightLightablesInAreaTaskGenerator((Vector2) args[0], (Vector2) args[1], host.getId().getId()));
							},
							true
						) {
							@Override
							public String getShortDescription() {
								return "Choose area";
							}
							@Override
							public boolean executionConditionMet() {
								return true;
							}
							@Override
							public boolean canCancel() {
								return true;
							}
							@Override
							public CursorBoundTask getImmediateTask() {
								return null;
							}
							@Override
							public void keyPressed(int keyCode) {
							}
						}
					);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			),
			new MenuItem(
				"Light selected",
				() -> {
					Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(
						new ChooseMultipleEntityCursorBoundTask<Prop, Integer>(true, Prop.class) {
							@Override
							public boolean canAdd(Prop f) {
								return Lightable.class.isAssignableFrom(f.getClass());
							}
							@Override
							public Integer transform(Prop f) {
								return f.id;
							}
							@Override
							public void renderUIGuide() {
								UserInterface.shapeRenderer.begin(ShapeType.Line);
								UserInterface.shapeRenderer.setColor(Color.RED);
								Gdx.gl20.glLineWidth(2f);
								for (int i : entities) {
									Prop p = Domain.getActiveWorld().props().getProp(i);
									Vector2 position = p.position;

									UserInterface.shapeRenderer.rect(
										worldToScreenX(position.x) - p.width/2,
										worldToScreenY(position.y),
										p.width,
										p.height
									);

								}
								UserInterface.shapeRenderer.end();
							}
							@Override
							public boolean executionConditionMet() {
								for (Prop prop : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, getMouseWorldX(), getMouseWorldY())) {
									if (Lightable.class.isAssignableFrom(prop.getClass())) {
										return true;
									}
								}
								return false;
							}
							@Override
							public String getShortDescription() {
								return "Select lightables";
							}
							@Override
							public void keyPressed(int keyCode) {
								if (keyCode == Keys.ENTER) {
									routine.setAiTaskGenerator(new LightSelectedLightablesTaskGenerator(host.getId().getId(), entities, host.getWorldId()));
									Wiring.injector().getInstance(BloodAndMithrilClientInputProcessor.class).setCursorBoundTask(null);
								}
							}
						}
					);
				},
				Color.ORANGE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return getContextMenu(routine, host);
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
		ContextMenu contextMenu = getContextMenu(routine, host);

		final EntityVisible identificationFunction = routine.getIdentificationFunction();
		if (Lightable.class.isAssignableFrom(identificationFunction.getEntity().a)) {
			contextMenu.addFirst(
				new MenuItem(
					"Visible lightable entity",
					() -> {
						routine.setAiTaskGenerator(new GenerateLightAnyVisibleLightables(host.getId().getId()));
					},
					Color.MAGENTA,
					Color.GREEN,
					Color.GRAY,
					null
				)
			);
		}
		return contextMenu;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return getContextMenu(routine, host);
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return getContextMenu(routine, host);
	}


	public static class GenerateLightAnyVisibleLightables extends TaskGenerator {
		private static final long serialVersionUID = 1898797069712115415L;
		private int individualId;

		public GenerateLightAnyVisibleLightables(int individualId) {
			this.individualId = individualId;
		}

		@Override
		public AITask apply(Object input) {
			if (!(input instanceof Lightable)) {
				return null;
			}

			try {
				Individual individual = Domain.getIndividual(individualId);
				if (individual.getFireLighter() == null || !individual.isAlive()) {
					return null;
				}
				return new LightLightable(individual, (Lightable) input, true);
			} catch (NoTileFoundException e) {
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
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.setColor(Color.GREEN);
			Individual attacker = Domain.getIndividual(individualId);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(attacker.getState().position.x) - attacker.getWidth()/2,
				worldToScreenY(attacker.getState().position.y),
				attacker.getWidth(),
				attacker.getHeight()
			);
			UserInterface.shapeRenderer.end();
		}
	}
}