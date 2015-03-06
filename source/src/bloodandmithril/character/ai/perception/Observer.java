package bloodandmithril.character.ai.perception;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
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

	public default void observe(int worldId, int hostId) {
		Vector2 eyes = getObservationPosition();
		float viewDistance = getViewDistance();

		World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getEntitiesWithinBounds(
			Individual.class,
			eyes.x - viewDistance,
			eyes.x + viewDistance,
			eyes.y + viewDistance,
			eyes.y - viewDistance
		).forEach(individualId -> {
			// Ray trace
			Individual toBeObserved = Domain.getIndividual(individualId);
			Individual host = Domain.getIndividual(hostId);
			if (toBeObserved == null || toBeObserved.getId().getId() == hostId || !(toBeObserved instanceof Visible)) {
				return;
			}

			if (canSee(((Visible)toBeObserved), world)) {
				host.getAI().addStimulus(new IndividualSighted(toBeObserved.getState().position, toBeObserved.getId().getId()));
			}
		});
	}
	
	/**
	 * @return whether this {@link Observer} can see a {@link Visible}
	 */
	public default boolean canSee(Visible visible, World world) {
		if (!visible.isVisible()) {
			return false;
		}
		
		Vector2 eyes = getObservationPosition();
		float viewDistance = getViewDistance();
		
		for (Vector2 visibilityCheckLocation : visible.getVisibleLocations()) {
			float dist = visibilityCheckLocation.dst(eyes);
			if (dist > viewDistance) {
				continue;
			}

			boolean canSee = RayTracingVisibilityChecker.check(
				world.getTopography(),
				eyes,
				visibilityCheckLocation.cpy().sub(eyes).nor(),
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
	public Vector2 getFieldOfView();

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