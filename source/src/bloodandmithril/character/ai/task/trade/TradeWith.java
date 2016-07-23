package bloodandmithril.character.ai.task.trade;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITask;
import bloodandmithril.character.ai.task.compositeaitask.CompositeAITaskExecutor;
import bloodandmithril.character.ai.task.gotolocation.GoToLocation;
import bloodandmithril.character.ai.task.gotolocation.GoToMovingLocation;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.Prop.ReturnPropPosition;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.util.SerializableFunction;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the proposee.
 * opening a {@link TradeWindow} with the proposee.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(CompositeAITaskExecutor.class)
public class TradeWith extends CompositeAITask {

	private static final long serialVersionUID = -4098496856332182431L;

	/**
	 * Overloaded constructor
	 */
	public TradeWith(final Individual proposer, final Container proposee, final int connectionId) {
		super(proposer.getId(), "Trading");

		SerializableFunction<Vector2> function = null;

		if (proposee instanceof Individual) {
			function = new ReturnIndividualPosition((Individual) proposee);
		} else {
			function = new ReturnPropPosition((Prop) proposee);;
		}

		appendTask(new GoToMovingLocation(
			proposer.getId(),
			function,
			50f
		));

		if (ClientServerInterface.isServer()) {
			appendTask(
				new Trade(hostId, proposer, proposee, connectionId)
			);
		} else {
			appendTask(
				new Trade(hostId, proposer, proposee)
			);
		}
	}

	/**
	 * Constructor
	 */
	public TradeWith(final Individual proposer, final Container proposee) {
		super(proposer.getId(), "Trading");

		SerializableFunction<Vector2> location = null;

		if (proposee instanceof Prop) {
			location = new ReturnPropPosition((Prop) proposee);;
		} else if (proposee instanceof Individual) {
			location = new ReturnIndividualPosition((Individual) proposee);
		}

		setCurrentTask(new GoToMovingLocation(
			proposer.getId(),
			location,
			50f
		));

		appendTask(
			new Trade(hostId, proposer, proposee)
		);
	}


	@ExecutedBy(TradeExecutor.class)
	public static class Trade extends AITask {
		private static final long serialVersionUID = 4644624691451364142L;

		final Individual proposer;
		final Container proposee;
		final int connectionId;

		/**
		 * Constructor
		 */
		protected Trade(final IndividualIdentifier hostId, final Individual proposer, final Container proposee, final int connectionId) {
			super(hostId);
			this.proposer = proposer;
			this.proposee = proposee;
			this.connectionId = connectionId;
		}

		/**
		 * Constructor
		 */
		protected Trade(final IndividualIdentifier hostId, final Individual proposer, final Container proposee) {
			super(hostId);
			this.proposer = proposer;
			this.proposee = proposee;
			this.connectionId = -1;
		}

		@Override
		public String getShortDescription() {
			return "Trading";
		}
	}
}