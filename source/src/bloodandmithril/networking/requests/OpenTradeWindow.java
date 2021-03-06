package bloodandmithril.networking.requests;

import com.google.inject.Inject;

import bloodandmithril.character.ai.task.trade.TradeExecutor;
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
 * {@link Request} to open a trade window
 */
@Copyright("Matthew Peck 2014")
public class OpenTradeWindow implements Request {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9173133908475415714L;
	private final int proposerId;
	private final TradeEntity proposee;
	private final int proposeeId;

	/**
	 * Constructor
	 */
	public OpenTradeWindow(int proposerId, TradeEntity proposee, int proposeeId) {
		this.proposerId = proposerId;
		this.proposee = proposee;
		this.proposeeId = proposeeId;
	}


	@Override
	public Responses respond() {
		Responses responses = new Response.Responses(false);

		responses.add(
			new SynchronizeIndividual.SynchronizeIndividualResponse(
				proposerId, System.currentTimeMillis()
			)
		);

		if (proposee == TradeEntity.INDIVIDUAL) {
			responses.add(
				new SynchronizeIndividual.SynchronizeIndividualResponse(
					proposerId, System.currentTimeMillis()
				)
			);
		} else {
			responses.add(
				new SynchronizePropRequest.SynchronizePropResponse(
					Domain.getWorld(Domain.getIndividual(proposerId).getWorldId()).props().getProp(proposeeId)
				)
			);
		}

		responses.add(
			new OpenTradeWindowResponse(
				proposerId,
				proposee,
				proposeeId
			)
		);

		return responses;
	}


	@Override
	public boolean tcp() {
		return true;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}


	public static class OpenTradeWindowResponse implements Response {
		private static final long serialVersionUID = 4563938302580135709L;
		
		@Inject private transient TradeExecutor tradeExecutor;
		
		private final int proposerId;
		private final TradeEntity proposeeEntity;
		private final int proposeeId;

		public OpenTradeWindowResponse(int proposerId, TradeEntity proposee, int proposeeId) {
			this.proposerId = proposerId;
			this.proposeeEntity = proposee;
			this.proposeeId = proposeeId;
		}

		@Override
		public void acknowledge() {
			Individual proposer = Domain.getIndividual(proposerId);
			if (proposeeEntity == TradeEntity.INDIVIDUAL) {
				Individual proposee = Domain.getIndividual(proposeeId);
				tradeExecutor.openTradeWindowWithIndividual(proposer, proposee);
			} else {
				Prop proposee = Domain.getWorld(proposer.getWorldId()).props().getProp(proposeeId);
				if (proposee instanceof Prop && proposee instanceof Container) {
					tradeExecutor.openTradeWindowWithProp(proposer, (Container) proposee);
				}
			}
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