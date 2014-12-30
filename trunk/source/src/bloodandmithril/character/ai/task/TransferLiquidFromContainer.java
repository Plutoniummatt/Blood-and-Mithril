package bloodandmithril.character.ai.task;

import static bloodandmithril.character.ai.task.GoToLocation.goTo;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.furniture.LiquidContainerProp;
import bloodandmithril.world.Domain;

/**
 * Task that instructs an individual to go to a {@link LiquidContainerProp} and open a liquid transfer window
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TransferLiquidFromContainer extends CompositeAITask {
	private static final long serialVersionUID = 1558139648365726555L;
	private int containerId;

	/**
	 * Constructor
	 */
	public TransferLiquidFromContainer(Individual host, LiquidContainerProp container) {
		super(
			host.getId(),
			"Transfer liquids",
			goTo(
				host,
				host.getState().position.cpy(),
				new WayPoint(container.position, 32),
				false,
				32f,
				true
			)
		);
		this.containerId = container.id;
	}


	public class OpenTranferLiquidWindow extends AITask {
		private static final long serialVersionUID = -6337135568036226381L;
		boolean opened;

		/**
		 * Constructor
		 */
		protected OpenTranferLiquidWindow(IndividualIdentifier hostId) {
			super(hostId);
		}


		@Override
		public String getDescription() {
			return "Tranfering liquid";
		}


		@Override
		public boolean isComplete() {
			return opened;
		}


		@Override
		public boolean uponCompletion() {
			return false;
		}


		@Override
		public void execute(float delta) {
			if (Domain.getIndividual(hostId.getId()).getDistanceFrom(Domain.getWorld(getHost().getWorldId()).props().getProp(containerId).position) > 64f) {
				return;
			}

			if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
			} else if (ClientServerInterface.isClient()) {
			}

			opened = true;
			Domain.getIndividual(hostId.getId()).clearCommands();
		}
	}
}