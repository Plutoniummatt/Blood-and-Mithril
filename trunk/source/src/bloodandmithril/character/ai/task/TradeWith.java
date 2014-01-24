package bloodandmithril.character.ai.task;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Chest.ChestContainer;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * A {@link CompositeAITask} comprising of:
 *
 * {@link GoToLocation} of the proposee.
 * opening a {@link TradeWindow} with the proposee.
 *
 * @author Matt
 */
public class TradeWith extends CompositeAITask {
	private static final long serialVersionUID = -4098496856332182431L;

	/**
	 * Overloaded constructor
	 */
	public TradeWith(final Individual proposer, final Container proposee, int connectionId) {
		super(proposer.id, "Trading");

		Vector2 location = null;

		if (proposee instanceof ChestContainer) {
			location = ((ChestContainer) proposee).getPositionOfChest();
		} else if (proposee instanceof Individual) {
			location = ((Individual) proposee).state.position;
		}

		currentTask = new GoToMovingLocation(
			proposer.id,
			location,
			50f
		);

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
		super(proposer.id, "Trading");

		Vector2 location = null;

		if (proposee instanceof ChestContainer) {
			location = ((ChestContainer) proposee).getPositionOfChest();
		} else if (proposee instanceof Individual) {
			location = ((Individual) proposee).state.position;
		}

		currentTask = new GoToMovingLocation(
			proposer.id,
			location,
			50f
		);

		appendTask(
			new Trade(hostId, proposer, proposee)
		);
	}


	public static class Trade extends AITask {

		private final Individual proposer;

		private final Container proposee;

		private final int connectionId;

		private static final long serialVersionUID = 4644624691451364142L;

		/**
		 * Constructor
		 */
		protected Trade(IndividualIdentifier hostId, Individual proposer, Container proposee, int connectionId) {
			super(hostId);
			this.proposer = proposer;
			this.proposee = proposee;
			this.connectionId = connectionId;
		}

		/**
		 * Constructor
		 */
		protected Trade(IndividualIdentifier hostId, Individual proposer, Container proposee) {
			super(hostId);
			this.proposer = proposer;
			this.proposee = proposee;
			this.connectionId = -1;
		}

		@Override
		public void uponCompletion() {
		}

		@Override
		public boolean isComplete() {
			return proposer.ai.getCurrentTask() instanceof Trading;
		}

		@Override
		public String getDescription() {
			return "Trading";
		}

		@Override
		public void execute() {

			if (proposee instanceof Individual) {
				Individual proposeeCasted = (Individual)proposee;

				if (proposer.getDistanceFrom(proposeeCasted.state.position) > 64f) {
					return;
				}

				if (ClientServerInterface.isServer() && !ClientServerInterface.isClient()) {
					ClientServerInterface.SendNotification.notifyTradeWindowOpen(proposer.id.id, TradeEntity.INDIVIDUAL, ((Individual) proposee).id.id, connectionId);
				} else if (ClientServerInterface.isClient()) {
					openTradeWindowWithIndividual(proposer, proposeeCasted);
				}

				proposer.clearCommands();
				proposer.ai.setCurrentTask(new Trading(proposer.id, ((Individual) proposee).id.id, TradeEntity.INDIVIDUAL));
				proposeeCasted.clearCommands();
				proposeeCasted.ai.setCurrentTask(new Trading(proposeeCasted.id, proposer.id.id, TradeEntity.INDIVIDUAL));
			} else if (proposee instanceof ChestContainer) {

				if (proposer.getDistanceFrom(((ChestContainer)proposee).getPositionOfChest()) > 64f) {
					return;
				}

				if (ClientServerInterface.isServer()) {
					ClientServerInterface.SendNotification.notifyTradeWindowOpen(proposer.id.id, TradeEntity.PROP, ((ChestContainer) proposee).propId, connectionId);
				} else if (ClientServerInterface.isClient()) {
					openTradeWindowWithProp(proposer, proposee);
				}

				proposer.clearCommands();
				proposer.ai.setCurrentTask(new Trading(proposer.id, ((ChestContainer) proposee).propId, TradeEntity.PROP));
			}
		}
	}


	/**
	 * Opens a {@link TradeWindow} with a {@link Prop} that has a {@link Container}
	 */
	public static void openTradeWindowWithProp(Individual proposer, Container prop) {
		UserInterface.addLayeredComponentUnique(
			new TradeWindow(
				BloodAndMithrilClient.WIDTH/2 - 450,
				BloodAndMithrilClient.HEIGHT/2 + 150,
				900,
				300,
				proposer.id.getSimpleName() + " interacting with pine chest",
				true,
				900,
				300,
				true,
				proposer,
				prop
			),
			proposer.id.getSimpleName() + " interacting with pine chest"
		);
	}


	/**
	 * Opens a {@link TradeWindow} with another {@link Individual}
	 */
	public static void openTradeWindowWithIndividual(Individual proposer, Individual proposeeCasted) {
		for (Component component : Lists.newArrayList(UserInterface.layeredComponents)) {
			if (component instanceof Window) {
				if (((Window)component).title.equals(proposer.id.getSimpleName() + " - Inventory") ||
						((Window)component).title.equals(proposeeCasted.id.getSimpleName() + " - Inventory")) {
					UserInterface.removeLayeredComponent(component);
				}
			}
		}

		UserInterface.addLayeredComponentUnique(
			new TradeWindow(
				BloodAndMithrilClient.WIDTH / 2 - 450,
				BloodAndMithrilClient.HEIGHT / 2 + 150,
				900,
				300,
				"Trade between " + proposer.id.firstName + " and " + proposeeCasted.id.firstName,
				true,
				900,
				300,
				true,
				proposer,
				proposeeCasted
			),
			"Trade between " + proposeeCasted.id.firstName + " and " + proposer.id.firstName
		);
	}
}