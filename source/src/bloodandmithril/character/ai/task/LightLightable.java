package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.TaskGenerator;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.item.FireLighter;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.google.inject.Inject;

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


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine routine) {
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
	}
}