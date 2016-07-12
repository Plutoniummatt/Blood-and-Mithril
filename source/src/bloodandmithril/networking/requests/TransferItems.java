package bloodandmithril.networking.requests;

import java.util.HashMap;

import com.google.inject.Inject;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.TradeService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} for transfer of items between two {@link Container}s
 */
@Copyright("Matthew Peck 2014")
public class TransferItems implements Request {

	@Inject
	private transient TradeService tradeService;

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
		final HashMap<Item, Integer> proposerItemsToTransfer, final int proposerId,
		final HashMap<Item, Integer> proposeeItemsToTransfer, final TradeEntity proposeeEntityType, final int proposeeId,
		final int client) {
		this.proposerItemsToTransfer = proposerItemsToTransfer;
		this.proposerId = proposerId;
		this.proposeeItemsToTransfer = proposeeItemsToTransfer;
		this.proposeeEntityType = proposeeEntityType;
		this.proposeeId = proposeeId;
		this.client = client;
	}


	@Override
	public Responses respond() {
		final Responses response = new Responses(true);

		Individual proposer;
		Container proposee;

		proposer = Domain.getIndividual(proposerId);
		response.add(new SynchronizeIndividual.SynchronizeIndividualResponse(proposer.getId().getId(), System.currentTimeMillis()));

		switch(proposeeEntityType) {
		case INDIVIDUAL:
			proposee = Domain.getIndividual(proposeeId);
			response.add(new SynchronizeIndividual.SynchronizeIndividualResponse(((Individual)proposee).getId().getId(), System.currentTimeMillis()));
			break;

		case PROP:
			final Prop prop = Domain.getWorld(proposer.getWorldId()).props().getProp(proposeeId);
			proposee = (Container) prop;
			response.add(new SynchronizePropRequest.SynchronizePropResponse(prop));
			break;

		default:
			throw new RuntimeException("Unknown Entity");
		}

		tradeService.transferItems(proposerItemsToTransfer, proposer, proposeeItemsToTransfer, proposee);
		response.add(new TransferItemsResponse(client));

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
		public TransferItemsResponse(final int client) {
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


	public enum TradeEntity {
		INDIVIDUAL, PROP
	}
}