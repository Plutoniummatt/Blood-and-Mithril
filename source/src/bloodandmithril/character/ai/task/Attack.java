package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.world.Domain;

/**
 * An {@link AITask} that tells the host to attack something
 *
 * @author Matt
 */
public class Attack extends CompositeAITask {
	private static final long serialVersionUID = 8988728685586897479L;

	/**
	 * Constructor
	 */
	public Attack(Individual host, Individual tobeAttacked) {
		super(host.getId(), "Attacking");

		setCurrentTask(new GoToMovingLocation(
			host.getId(),
			tobeAttacked.getState().position,
			host.getCurrentAttackRange()
		));

		appendTask(new Strike(host, tobeAttacked));
	}


	public static class Strike extends AITask {
		private static final long serialVersionUID = 2202041223279146589L;
		
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
		public void execute(float delta) {
			Domain.getIndividuals().get(hostId.getId()).attack(tobeAttacked);
			Domain.getIndividuals().get(hostId.getId()).clearCommands();
			attacked = true;
		}
	}
}