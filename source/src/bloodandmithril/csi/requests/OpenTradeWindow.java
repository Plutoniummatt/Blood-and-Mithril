package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Chest;
import bloodandmithril.world.GameWorld;

import com.google.common.collect.Lists;

/**
 * {@link Request} to open a trade window
 */
public class OpenTradeWindow implements Request {

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
	public List<Response> respond() {
		List<Response> response = Lists.newArrayList();

		response.add(
			new SynchronizeIndividual.SynchronizeIndividualResponse(
				GameWorld.individuals.get(proposerId)
			)
		);

		if (proposee == TradeEntity.INDIVIDUAL) {
			response.add(
				new SynchronizeIndividual.SynchronizeIndividualResponse(
					GameWorld.individuals.get(proposerId)
				)
			);
		} else {
			// TODO sync prop
		}

		response.add(
			new OpenTradeWindowResponse(
				proposerId,
				proposee,
				proposeeId
			)
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


	public static class OpenTradeWindowResponse implements Response {

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
			Individual proposer = GameWorld.individuals.get(proposerId);
			if (proposeeEntity == TradeEntity.INDIVIDUAL) {
				Individual proposee = GameWorld.individuals.get(proposeeId);
				TradeWith.openTradeWindowWithIndividual(proposer, proposee);
			} else {
				Prop proposee = GameWorld.props.get(proposeeId);
				if (proposee instanceof Chest) {
					TradeWith.openTradeWindowWithProp(proposer, ((Chest) proposee).container);
				}
			}
		}
	}
}