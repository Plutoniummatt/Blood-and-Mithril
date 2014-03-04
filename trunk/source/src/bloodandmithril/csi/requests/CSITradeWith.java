package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.ConstructionWithContainer;
import bloodandmithril.world.GameWorld;

/**
 * Request to tell the server that an {@link Individual} would like to {@link TradeWith} something.
 */
public class CSITradeWith implements Request {

	private final int proposerId;
	private final TradeEntity proposee;
	private final int proposeeId;
	private final int connectionId;

	/**
	 * Constructor
	 */
	public CSITradeWith(int proposerId, TradeEntity proposee, int proposeeId, int connectionId) {
		this.proposerId = proposerId;
		this.proposee = proposee;
		this.proposeeId = proposeeId;
		this.connectionId = connectionId;
	}


	@Override
	public Responses respond() {
		Responses response = new Response.Responses(false);

		Individual proposer = GameWorld.individuals.get(proposerId);
		Container proposee = null;

		if (this.proposee == TradeEntity.INDIVIDUAL) {
			proposee = GameWorld.individuals.get(proposeeId);
		} else {
			Prop prop = GameWorld.props.get(proposeeId);
			if (prop instanceof ConstructionWithContainer) {
				proposee = ((ConstructionWithContainer) prop).container;
			}
		}

		proposer.getAI().setCurrentTask(
			new TradeWith(proposer, proposee, connectionId)
		);

		return response;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class CSITradeWithResponse implements Response {
		@Override
		public void acknowledge() {
			// Do nothing
		}

		@Override
		public int forClient() {
			return -1;
		}

		@Override
		public void prepare() {
		}
	}
}