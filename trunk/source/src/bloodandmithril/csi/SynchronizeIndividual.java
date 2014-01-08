package bloodandmithril.csi;

import java.util.Map;

import bloodandmithril.character.Individual;
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
			return new SynchronizeIndividualResponse(GameWorld.individuals);
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

		private final Map<Integer, Individual> individuals;

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
		public SynchronizeIndividualResponse(Map<Integer, Individual> individuals) {
			this.individual = null;
			this.individuals = individuals;
		}

		@Override
		public void acknowledge() {
			if (this.individual == null) {
				GameWorld.selectedIndividuals.clear();
				for (Individual indi : individuals.values()) {
					if (GameWorld.individuals.get(indi.id.id) != null && GameWorld.individuals.get(indi.id.id).selected) {
						indi.selected = true;
						GameWorld.selectedIndividuals.add(indi);
					} else {
						indi.selected = false;
					}
				}
				GameWorld.individuals.clear();
				GameWorld.individuals.putAll(individuals);
				System.out.println("Synchronized individuals");
			} else {
				Individual removed = GameWorld.individuals.remove(individual.id.id);
				if (removed != null) {
					GameWorld.individuals.put(individual.id.id, individual);
					if (GameWorld.selectedIndividuals.remove(removed)) {
						GameWorld.selectedIndividuals.add(individual);
					}
					System.out.println("Received data for individual: " + individual.id.getSimpleName());
				}
			}
		}
	}
}