package bloodandmithril.character.ai.task;

import java.util.Comparator;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.FueledCraftingStation;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.FueledCraftingStationFuelWindow;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

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

	private static Comparator<Item> sortOrder = (o1, o2) -> {
		return o1.getSingular(false).compareTo(o2.getSingular(false));
	};

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

		appendTask(new GoToMovingLocation(
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
		public boolean uponCompletion() {
			return false;
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
			Prop prop = Domain.getWorld(proposer.getWorldId()).props().getProp(((Prop) container).id);
			if (prop instanceof FueledCraftingStation) {
				UserInterface.addLayeredComponentUnique(
					new FueledCraftingStationFuelWindow(
						BloodAndMithrilClient.WIDTH/2 - 450,
						BloodAndMithrilClient.HEIGHT/2 + 150,
						900,
						300,
						proposer.getId().getSimpleName() + " interacting with container",
						true,
						900,
						300,
						proposer,
						(FueledCraftingStation) prop
					)
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
						(Container) prop,
						sortOrder
					)
				);
			}
		}
	}


	/**
	 * Opens a {@link TradeWindow} with another {@link Individual}
	 */
	public static void openTradeWindowWithIndividual(Individual proposer, Individual proposeeCasted) {
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
				proposeeCasted,
				sortOrder
			)
		);
	}
}