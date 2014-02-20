package bloodandmithril.csi.requests;

import java.util.HashMap;
import java.util.LinkedList;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.Container;
import bloodandmithril.item.Item;
import bloodandmithril.item.TradeService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.ConstructionWithContainer;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.world.GameWorld;

/**
 * A {@link Request} for transfer of items between two {@link Container}s
 */
public class TransferItems implements Request {

	private final HashMap<Item, Integer> proposerItemsToTransfer;
	private final HashMap<Item, Integer> proposeeItemsToTransfer;
	private final TradeEntity proposeeEntityType;
	private final int proposerId;
	private final int proposeeId;
	private final int client;

	/**
	 * Constructor
	 */
	public TransferItems(
		HashMap<Item, Integer> proposerItemsToTransfer, int proposerId,
		HashMap<Item, Integer> proposeeItemsToTransfer, TradeEntity proposeeEntityType, int proposeeId,
		int client) {
		this.proposerItemsToTransfer = proposerItemsToTransfer;
		this.proposerId = proposerId;
		this.proposeeItemsToTransfer = proposeeItemsToTransfer;
		this.proposeeEntityType = proposeeEntityType;
		this.proposeeId = proposeeId;
		this.client = client;
	}


	@Override
	public Responses respond() {
		Responses response = new Responses(true, new LinkedList<Response>());

		Individual proposer;
		Container proposee;

		proposer = GameWorld.individuals.get(proposerId);
		response.responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(proposer.getId().getId(), System.currentTimeMillis()));

		switch(proposeeEntityType) {
		case INDIVIDUAL:
			proposee = GameWorld.individuals.get(proposeeId);
			response.responses.add(new SynchronizeIndividual.SynchronizeIndividualResponse(((Individual)proposee).getId().getId(), System.currentTimeMillis()));
			break;

		case PROP:
			Prop prop = GameWorld.props.get(proposerId);
			proposee = ((ConstructionWithContainer) prop).container;
			response.responses.add(new SynchronizePropRequest.SynchronizePropResponse(prop));
			break;

		default:
			throw new RuntimeException("Unknown Entity");
		}

		TradeService.transferItems(proposerItemsToTransfer, proposer, proposeeItemsToTransfer, proposee);
		response.responses.add(new TransferItemsResponse(client));

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
		public final int client;

		/**
		 * Constructor
		 */
		public TransferItemsResponse(int client) {
			this.client = client;
		}

		@Override
		public void acknowledge() {
			// Need to notify all clients to refresh inventory windows and trade windows
			ClientServerInterface.SendRequest.sendRefreshItemWindowsRequest();
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}


	public static class RefreshWindows implements Request {
		@Override
		public Responses respond() {
			Responses responses = new Responses(false, new LinkedList<Response>());
			responses.responses.add(new RefreshWindowsResponse());
			return responses;
		}

		@Override
		public boolean tcp() {
			return true;
		}

		@Override
		public boolean notifyOthers() {
			return true;
		}
	}


	public static class RefreshWindowsResponse implements Response {
		@Override
		public void acknowledge() {
			UserInterface.refreshInventoryWindows();
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}


	public enum TradeEntity {
		INDIVIDUAL, PROP
	}
}