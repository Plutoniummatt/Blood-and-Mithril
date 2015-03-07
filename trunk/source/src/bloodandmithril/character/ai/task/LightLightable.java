package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.perception.Visible.getVisible;
import bloodandmithril.audio.SoundService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.pathfinding.PathFinder;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.equipment.misc.FlintAndFiresteel;
import bloodandmithril.prop.Lightable;
import bloodandmithril.prop.Prop;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Task that instructs the host to light a {@link Lightable}
 *
 * @author Matt
 */
public class LightLightable extends CompositeAITask {
	private static final long serialVersionUID = -2379179198784328909L;

	private boolean auto;
	private int lightableId;

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
		public String getDescription() {
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
				if (host.has(new FlintAndFiresteel()) > 0) {
					ParticleService.parrySpark(((Prop) lightable).position.cpy().add(0, 7), new Vector2(), Depth.MIDDLEGROUND, Color.WHITE, Color.WHITE, 100, true);
					SoundService.play(SoundService.flint, ((Prop) lightable).position, true, getVisible(lightable));
					lightable.light();
				} else {
					host.speak("I need fire lighting equipment", 2000);
				}
				lit = true;
			}
		}
	}
}