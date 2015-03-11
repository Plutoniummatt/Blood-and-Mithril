package bloodandmithril.character.ai.perception;

import static java.lang.Math.acos;
import static java.lang.Math.toDegrees;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

import com.badlogic.gdx.math.Vector2;

/**
 * The ability to see
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Observer {

	public static Comparator<Integer> randomComparator = (i1, i2) -> {
		return Util.getRandom().nextBoolean() ? -1 : 1;
	};

	/**
	 * Observes nearby entities.
	 *
	 * @param worldId of the world to observe
	 * @param hostId of the observer
	 * @param entityCap to cap the number of observed entities, to cut down on performance drawbacks
	 */
	public default void observe(int worldId, int hostId, int entityCap) {
		Vector2 eyes = getObservationPosition();
		float viewDistance = getViewDistance();

		World world = Domain.getWorld(worldId);
		List<Integer> entitiesWithinBounds = world.getPositionalIndexMap().getEntitiesWithinBounds(
			Individual.class,
			eyes.x - viewDistance,
			eyes.x + viewDistance,
			eyes.y + viewDistance,
			eyes.y - viewDistance
		);
		Collections.shuffle(entitiesWithinBounds);
		int index = 0;

		for (Integer id : entitiesWithinBounds) {
			if (index >= entityCap) {
				break;
			}

			// Ray trace
			Individual toBeObserved = Domain.getIndividual(id);
			Individual host = Domain.getIndividual(hostId);
			if (toBeObserved == null || toBeObserved.getId().getId() == hostId || !(toBeObserved instanceof Visible)) {
				return;
			}

			if (canSee((Visible)toBeObserved, world)) {
				host.getAI().addStimulus(new IndividualSighted(toBeObserved.getState().position, toBeObserved.getId().getId()));
			}
			index++;
		}
	}

	/**
	 * @return whether this {@link Observer} can see a {@link Visible}
	 */
	public default boolean canSee(Visible visible, World world) {
		if (!visible.isVisible()) {
			return false;
		}
		
		Vector2 viewingDirection = getObservationDirection().cpy().nor();
		Vector2 eyes = getObservationPosition();
		float viewDistance = getViewDistance();

		for (Vector2 visibilityCheckLocation : visible.getVisibleLocations()) {
			float dist = visibilityCheckLocation.dst(eyes);
			if (dist > viewDistance) {
				continue;
			}
			
			Vector2 targetDirection = visibilityCheckLocation.cpy().sub(eyes).nor();
			float angle = (float) toDegrees(acos(viewingDirection.dot(targetDirection)));
			if (angle > getFieldOfView() / 2f) {
				continue;
			}

			boolean canSee = RayTracingVisibilityChecker.check(
				world.getTopography(),
				eyes,
				targetDirection,
				dist
			);

			if (canSee) {
				return true;
			}
		}

		return false;
	}


	/**
	 * @return the position of the eyes
	 */
	public Vector2 getObservationPosition();

	/**
	 * @return the direction the eyes are facing
	 */
	public Vector2 getObservationDirection();

	/**
	 * @return the FOV, in degrees
	 */
	public float getFieldOfView();

	/**
	 * Implementation specific reaction method
	 */
	public void reactToSightStimulus(SightStimulus stimulus);

	/**
	 * @return the maximum view distance
	 */
	public float getViewDistance();


	public static class IndividualSighted implements SightStimulus {
		private static final long serialVersionUID = -8367150474646829719L;
		private int sightedIndividualId;
		private Vector2 location;

		/**
		 * Constructor
		 */
		public IndividualSighted(Vector2 location, int sightedIndividualId) {
			this.location = location;
			this.sightedIndividualId = sightedIndividualId;
		}

		@Override
		public Vector2 getSightLocation() {
			return location;
		}

		public int getSightedIndividualId() {
			return sightedIndividualId;
		}
	}
}