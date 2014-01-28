package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.world.GameWorld;

/**
 * An {@link AITask} that tells the host to attack something
 *
 * @author Matt
 */
public class Attack extends CompositeAITask {

	/**
	 * Constructor
	 */
	public Attack(Individual host, Individual tobeAttacked) {
		super(host.getId(), "Attacking");

		setCurrentTask(new GoToMovingLocation(
			host.getId(),
			tobeAttacked.getState().position,
			50f
		));

		appendTask(new Strike(host, tobeAttacked));
	}


	public static class Strike extends AITask {

		private final Individual tobeAttacked;
		private boolean attacked = false;

		/** Constructor */
		public Strike(Individual host, Individual tobeAttacked) {
			super(host.getId());
			this.tobeAttacked = tobeAttacked;
		}


		@Override
		public String getDescription() {
			return "Striking";
		}


		@Override
		public boolean isComplete() {
			return attacked;
		}


		@Override
		public void uponCompletion() {
			// Do nothing
		}


		@Override
		public void execute() {
			GameWorld.individuals.get(hostId.getId()).attack(tobeAttacked);
			attacked = true;
		}
	}
}