package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

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

		Individual proposer = Domain.getIndividuals().get(proposerId);
		Container proposee = null;

		if (this.proposee == TradeEntity.INDIVIDUAL) {
			proposee = Domain.getIndividuals().get(proposeeId);
		} else {
			Prop prop = Domain.getProps().get(proposeeId);
			if (prop instanceof Container) {
				proposee = (Container) prop;
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
}