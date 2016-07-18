package bloodandmithril.character.ai.task;

import java.util.Comparator;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.ComparisonChain;

import bloodandmithril.character.ai.AIProcessor.ReturnIndividualPosition;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.Prop.ReturnPropPosition;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the proposee.
 * opening a {@link TradeWindow} with the proposee.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class TradeWith extends CompositeAITask {

	private static final long serialVersionUID = -4098496856332182431L;

	public static Comparator<Item> sortOrder = new Comparator<Item>() {
		@Override
		public int compare(final Item o1, final Item o2) {
			return ComparisonChain.start()
					.compare(o1.getType().getColor().toIntBits(), o2.getType().getColor().toIntBits())
					.compare(o1.getSingular(false).toUpperCase(), o2.getSingular(false).toUpperCase())
					.result();
		}
	};

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


	public static class Trade extends AITask {
		private static final long serialVersionUID = 4644624691451364142L;

		private final Individual proposer;

		private final Container proposee;

		private final int connectionId;

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
		public boolean uponCompletion() {
			return false;
		}

		@Override
		public boolean isComplete() {
			if (proposee instanceof Individual) {
				if (!((Individual) proposee).isAlive()) {
					return true;
				}
			}

			return proposer.getAI().getCurrentTask() instanceof Trading;
		}

		@Override
		public String getShortDescription() {
			return "Trading";
		}

		@Override
		protected void internalExecute(final float delta) {
			if (proposee.isLocked()) {
				return;
			}

			if (proposee instanceof Individual) {
				final Individual proposeeCasted = (Individual)proposee;

				if (proposer.getDistanceFrom(proposeeCasted.getState().position) > 64f) {
					return;
				}

				if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
					ClientServerInterface.SendNotification.notifyTradeWindowOpen(proposer.getId().getId(), TradeEntity.INDIVIDUAL, ((Individual) proposee).getId().getId(), connectionId);
				} else if (ClientServerInterface.isClient()) {
					openTradeWindowWithIndividual(proposer, proposeeCasted);
				}

				proposer.clearCommands();
				proposer.getAI().setCurrentTask(new Trading(proposer.getId(), ((Individual) proposee).getId().getId(), TradeEntity.INDIVIDUAL));
				proposeeCasted.clearCommands();
				proposeeCasted.getAI().setCurrentTask(new Trading(proposeeCasted.getId(), proposer.getId().getId(), TradeEntity.INDIVIDUAL));
			} else if (proposee instanceof Prop) {

				if (proposer.getDistanceFrom(((Prop)proposee).position) > 64f) {
					return;
				}

				if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
					ClientServerInterface.SendNotification.notifyTradeWindowOpen(proposer.getId().getId(), TradeEntity.PROP, ((Prop) proposee).id, connectionId);
				} else if (ClientServerInterface.isClient()) {
					openTradeWindowWithProp(proposer, proposee);
				}

				proposer.clearCommands();
				proposer.getAI().setCurrentTask(new Trading(proposer.getId(), ((Prop) proposee).id, TradeEntity.PROP));
			}
		}
	}


	/**
	 * Opens a {@link TradeWindow} with a {@link Prop}  is also a {@link Container}
	 */
	public static void openTradeWindowWithProp(final Individual proposer, final Container container) {
		if (container instanceof Prop) {
			final Prop prop = Domain.getWorld(proposer.getWorldId()).props().getProp(((Prop) container).id);
				Wiring.injector().getInstance(UserInterface.class).addLayeredComponentUnique(
				new TradeWindow(
					proposer.getId().getSimpleName() + " interacting with container",
					true,
					proposer,
					(Container) prop,
					sortOrder
				)
			);
		}
	}


	/**
	 * Opens a {@link TradeWindow} with another {@link Individual}
	 */
	public static void openTradeWindowWithIndividual(final Individual proposer, final Individual proposeeCasted) {
		Wiring.injector().getInstance(UserInterface.class).addLayeredComponentUnique(
			new TradeWindow(
				"Trade between " + proposer.getId().getFirstName() + " and " + proposeeCasted.getId().getFirstName(),
				true,
				proposer,
				proposeeCasted,
				sortOrder
			)
		);
	}
}