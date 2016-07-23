package bloodandmithril.character.ai.task.transferliquid;

import static bloodandmithril.character.ai.task.gotolocation.GoToLocation.goTo;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.furniture.LiquidContainerProp;

/**
 * Task that instructs an individual to go to a {@link LiquidContainerProp} and open a liquid transfer window
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
public class TransferLiquidFromContainer extends CompositeAITask {
	private static final long serialVersionUID = 1558139648365726555L;
	int containerId;

	/**
	 * Constructor
	 */
	public TransferLiquidFromContainer(final Individual host, final LiquidContainerProp container) {
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


	@ExecutedBy(OpenTranferLiquidWindowExecutor.class)
	public class OpenTranferLiquidWindow extends AITask {
		private static final long serialVersionUID = -6337135568036226381L;
		boolean opened;

		/**
		 * Constructor
		 */
		protected OpenTranferLiquidWindow(final IndividualIdentifier hostId) {
			super(hostId);
		}
		
		
		TransferLiquidFromContainer getParent() {
			return TransferLiquidFromContainer.this;
		}


		@Override
		public String getShortDescription() {
			return "Tranfering liquid";
		}
	}
}