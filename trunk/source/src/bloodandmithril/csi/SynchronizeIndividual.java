package bloodandmithril.csi;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualState;
import bloodandmithril.world.GameWorld;

/**
 * Synchronizes an {@link Individual}
 *
 * @author Matt
 */
public class SynchronizeIndividual implements Request {

	/** ID of the individual to sync */
	private final int id;

	/** Type of request */
	private final IndividualSyncRequest request;

	/**
	 * Constructor
	 */
	public SynchronizeIndividual(int id, IndividualSyncRequest request) {
		this.id = id;
		this.request = request;
	}


	@Override
	public Response respond() {
		switch (request) {
		case STATE:
			return new SynchronizeIndividualResponse<IndividualState>(GameWorld.individuals.get(id).state);
		default:
			throw new IllegalStateException("Unrecognised request");
		}
	}


	/**
	 * Information request type
	 * @author Matt
	 */
	public enum IndividualSyncRequest {
		STATE
	}


	/**
	 * Response class of {@link SynchronizeIndividual}
	 *
	 * @author Matt
	 */
	public static class SynchronizeIndividualResponse<T> implements Response {

		public final T t;

		public SynchronizeIndividualResponse(T t) {
			this.t = t;
		}

		@Override
		public void acknowledge() {
			if (t instanceof IndividualState) {
				System.out.println(((IndividualState) t).position);
			}
		}
	}
}