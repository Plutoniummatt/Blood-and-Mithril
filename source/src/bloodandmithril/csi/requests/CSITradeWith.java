package bloodandmithril.csi.requests;

import java.util.List;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TradeWith;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.building.Chest;
import bloodandmithril.world.GameWorld;

import com.google.common.collect.Lists;

/**
 * Request to tell the server that an {@link Individual} would like to {@link TradeWith} something.
 */
public class CSITradeWith implements Request {

	private final int proposerId;
	private final TradeEntity proposee;
	private final int proposeeId;

	/**
	 * Constructor
	 */
	public CSITradeWith(int proposerId, TradeEntity proposee, int proposeeId) {
		this.proposerId = proposerId;
		this.proposee = proposee;
		this.proposeeId = proposeeId;
	}


	@Override
	public List<Response> respond() {
		List<Response> response = Lists.newArrayList();

		Individual proposer = GameWorld.individuals.get(proposerId);
		Container proposee = null;

		if (this.proposee == TradeEntity.INDIVIDUAL) {
			proposee = GameWorld.individuals.get(proposeeId);
		} else {
			Prop prop = GameWorld.props.get(proposeeId);
			if (prop instanceof Chest) {
				proposee = ((Chest) prop).container;
			}
		}

		proposer.ai.setCurrentTask(
			new TradeWith(proposer, proposee)
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
	}
}