package bloodandmithril.networking.requests;

import bloodandmithril.character.ai.task.trade.TradeWith;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.Request;
import bloodandmithril.networking.Response;
import bloodandmithril.networking.Response.Responses;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * Request to tell the server that an {@link Individual} would like to {@link TradeWith} something.
 */
@Copyright("Matthew Peck 2014")
public class CSITradeWith implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = -973354206062930233L;
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

		Individual proposer = Domain.getIndividual(proposerId);
		Container proposee = null;

		if (this.proposee == TradeEntity.INDIVIDUAL) {
			proposee = Domain.getIndividual(proposeeId);
		} else {
			Prop prop = Domain.getWorld(proposer.getWorldId()).props().getProp(proposeeId);
			if (prop instanceof Container) {
				proposee = (Container) prop;
			}
		}

		if (proposee != null) {
			proposer.getAI().setCurrentTask(
				new TradeWith(proposer, proposee, connectionId)
			);
		}

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