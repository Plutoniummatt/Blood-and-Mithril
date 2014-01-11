package bloodandmithril.csi.requests;

import java.util.HashMap;
import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.TradeService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Chest;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.Component;
import bloodandmithril.ui.components.window.InventoryWindow;
import bloodandmithril.ui.components.window.TradeWindow;
import bloodandmithril.world.GameWorld;

/**
 * A {@link Request} for transfer of items between two {@link Container}s
 */
public class TransferItems implements Request {

	private final HashMap<Item, Integer> proposerItemsToTransfer;
	private final HashMap<Item, Integer> proposeeItemsToTransfer;
	private final TradeEntity proposerEntityType;
	private final TradeEntity proposeeEntityType;
	private final int proposerId;
	private final int proposeeId;

	/**
	 * Constructor
	 */
	public TransferItems(
		HashMap<Item, Integer> proposerItemsToTransfer,
		TradeEntity proposerEntityType, int proposerId,
		HashMap<Item, Integer> proposeeItemsToTransfer,
		TradeEntity proposeeEntityType, int proposeeId) {
		this.proposerItemsToTransfer = proposerItemsToTransfer;
		this.proposerEntityType = proposerEntityType;
		this.proposerId = proposerId;
		this.proposeeItemsToTransfer = proposeeItemsToTransfer;
		this.proposeeEntityType = proposeeEntityType;
		this.proposeeId = proposeeId;
	}


	@Override
	public Responses respond() {
		Responses response = new Responses(true, new LinkedList<Response>());
		
		Container proposer, proposee;

		switch(proposerEntityType) {
		case INDIVIDUAL:
			proposer = GameWorld.individuals.get(proposerId);
			response.responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse((Individual)proposer));
		break;

		case PROP:
			Prop prop = GameWorld.props.get(proposerId);
			proposer = ((Chest) prop).container;
			// TODO Add prop sync to response list
		break;

		default:
			throw new RuntimeException("Unknown Entity");
		}

		switch(proposeeEntityType) {
		case INDIVIDUAL:
			proposee = GameWorld.individuals.get(proposeeId);
			response.responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse((Individual)proposee));
			break;

		case PROP:
			Prop prop = GameWorld.props.get(proposerId);
			proposee = ((Chest) prop).container;
			// TODO Add prop sync to response list
			break;

		default:
			throw new RuntimeException("Unknown Entity");
		}

		TradeService.transferItems(proposerItemsToTransfer, proposer, proposeeItemsToTransfer, proposee);
		response.responses.add(new TransferItemsResponse());

		return response;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return true;
	}


	public static class TransferItemsResponse implements Response {
		@Override
		public void acknowledge() {
			// Need to notify all clients to refresh inventory windows and trade windows
			for (Component component : UserInterface.layeredComponents) {
				if (component instanceof TradeWindow) {
					((TradeWindow) component).refresh();
				} else if (component instanceof InventoryWindow) {
					((InventoryWindow) component).refresh();
				}
			}
		}
	}


	public enum TradeEntity {
		INDIVIDUAL, PROP
	}
}