package bloodandmithril.csi;

import java.util.Set;

import bloodandmithril.character.Individual;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.GameWorld;

/**
 * Synchronizes an {@link Individual}
 *
 * @author Matt
 */
public class SynchronizeIndividual implements Request {

	/** ID of the individual to sync */
	private final int id;

	/**
	 * Constructor
	 */
	public SynchronizeIndividual(int id) {
		this.id = id;
	}


	/**
	 * Synchronize all individuals
	 */
	public SynchronizeIndividual() {
		this.id = -1;
	}


	@Override
	public Response respond() {
		if (id == -1) {
			return new SynchronizeIndividualResponse(GameWorld.individuals.keySet());
		}
		return new SynchronizeIndividualResponse(GameWorld.individuals.get(id));
	}


	/**
	 * Response class of {@link SynchronizeIndividual}
	 *
	 * @author Matt
	 */
	public static class SynchronizeIndividualResponse implements Response {

		private final Individual individual;

		private final Set<Integer> individuals;

		/**
		 * Synchronize single individual
		 */
		public SynchronizeIndividualResponse(Individual individual) {
			this.individual = individual;
			this.individuals = null;
		}

		/**
		 * Synchronize all individuals
		 */
		public SynchronizeIndividualResponse(Set<Integer> individuals) {
			this.individual = null;
			this.individuals = individuals;
		}

		@Override
		public void acknowledge() {
			if (this.individual == null) {
				for (Integer id : individuals) {
					ClientServerInterface.sendSynchronizeIndividualRequest(id);
				}
			} else {
				syncSingleIndividual();
			}
		}

		private void syncSingleIndividual() {
			Individual removed = GameWorld.individuals.remove(individual.id.id);
			if (removed != null) {
				GameWorld.individuals.put(individual.id.id, individual);
				if (GameWorld.selectedIndividuals.remove(removed)) {
					individual.selected = true;
					GameWorld.selectedIndividuals.add(individual);
				} else {
					individual.selected = false;
				}
				Logger.networkDebug("Received data for individual: " + individual.id.getSimpleName(), LogLevel.TRACE);
			}
		}
	}


	@Override
	public boolean tcp() {
		return false;
	}
}