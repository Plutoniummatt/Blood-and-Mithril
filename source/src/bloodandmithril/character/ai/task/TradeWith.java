package bloodandmithril.character.ai.task;

import bloodandmithril.character.Individual;
import bloodandmithril.character.Individual.IndividualIdentifier;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Construction;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.ConstructionWindow;
import bloodandmithril.ui.components.window.FurnaceWindow;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.Domain;

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
		super(proposer.getId(), "Trading");

		Vector2 location = null;

		if (proposee instanceof Individual) {
			location = ((Individual) proposee).getState().position;
		} else {
			location = ((Prop) proposee).position;
		}

		setCurrentTask(new GoToMovingLocation(
			proposer.getId(),
			location,
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

		Vector2 location = null;

		if (proposee instanceof Prop) {
			location = ((Prop) proposee).position;
		} else if (proposee instanceof Individual) {
			location = ((Individual) proposee).getState().position;
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
			return proposer.getAI().getCurrentTask() instanceof Trading;
		}

		@Override
		public String getDescription() {
			return "Trading";
		}

		@Override
		public void execute(float delta) {
			if (proposee.isLocked()) {
				return;
			}

			if (proposee instanceof Individual) {
				Individual proposeeCasted = (Individual)proposee;

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
	public static void openTradeWindowWithProp(Individual proposer, Container container) {
		if (container instanceof Prop) {
			Prop prop = Domain.getProps().get(((Prop) container).id);

			if (prop instanceof Construction) {
				if (((Construction) prop).getConstructionProgress() != 1f) {
					UserInterface.addLayeredComponentUnique(
						new ConstructionWindow(
							BloodAndMithrilClient.WIDTH/2 - 450,
							BloodAndMithrilClient.HEIGHT/2 + 150,
							900,
							300,
							proposer.getId().getSimpleName() + " interacting with container",
							true,
							900,
							300,
							proposer,
							(Construction) prop
						),
						proposer.getId().getSimpleName() + " interacting with container"
					);
					return;
				}
			}

			if (prop instanceof Furnace) {
				UserInterface.addLayeredComponentUnique(
					new FurnaceWindow(
						BloodAndMithrilClient.WIDTH/2 - 450,
						BloodAndMithrilClient.HEIGHT/2 + 150,
						900,
						300,
						proposer.getId().getSimpleName() + " interacting with container",
						true,
						900,
						300,
						proposer,
						(Furnace) prop
					),
					proposer.getId().getSimpleName() + " interacting with container"
				);
			} else {
				UserInterface.addLayeredComponentUnique(
					new TradeWindow(
						BloodAndMithrilClient.WIDTH/2 - 450,
						BloodAndMithrilClient.HEIGHT/2 + 150,
						900,
						300,
						proposer.getId().getSimpleName() + " interacting with container",
						true,
						900,
						300,
						proposer,
						(Container) prop
					),
					proposer.getId().getSimpleName() + " interacting with container"
				);
			}
		}
	}


	/**
	 * Opens a {@link TradeWindow} with another {@link Individual}
	 */
	public static void openTradeWindowWithIndividual(Individual proposer, Individual proposeeCasted) {
		for (Component component : Lists.newArrayList(UserInterface.layeredComponents)) {
			if (component instanceof Window) {
				if (((Window)component).title.equals(proposer.getId().getSimpleName() + " - Inventory") ||
						((Window)component).title.equals(proposeeCasted.getId().getSimpleName() + " - Inventory")) {
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
				"Trade between " + proposer.getId().getFirstName() + " and " + proposeeCasted.getId().getFirstName(),
				true,
				900,
				300,
				proposer,
				proposeeCasted
			),
			"Trade between " + proposeeCasted.getId().getFirstName() + " and " + proposer.getId().getFirstName()
		);
	}
}