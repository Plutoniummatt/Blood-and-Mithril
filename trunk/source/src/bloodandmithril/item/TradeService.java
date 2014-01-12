package bloodandmithril.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.building.Chest.ChestContainer;

/**
 * Evaluates a trade proposal.
 *
 * @author Matt
 */
public class TradeService {

	/**
	 * Evaluates a trade proposal
	 */
	public static boolean evaluate(Individual proposer, List<Item> tradeThis, Individual proposee, List<Item> forThis) {
		// TODO - Evaluate trade proposal
		return true;
	}


	/**
	 * The trade proposal was accepted by the proposee, this method transfers the {@link Item}s and finalizes the trade
	 */
	public synchronized static void transferItems(HashMap<Item, Integer> proposerItemsToTrade, Container proposer, HashMap<Item, Integer> proposeeItemsToTrade, Container proposee) {
		if (ClientServerInterface.isServer()) {
			for (Entry<Item, Integer> proposerToTradeItem : proposerItemsToTrade.entrySet()) {
				proposer.takeItem(proposerToTradeItem.getKey(), proposerToTradeItem.getValue());
				proposee.giveItem(proposerToTradeItem.getKey(), proposerToTradeItem.getValue());
			}
			for (Entry<Item, Integer> proposeeToTradeItem : proposeeItemsToTrade.entrySet()) {
				proposee.takeItem(proposeeToTradeItem.getKey(), proposeeToTradeItem.getValue());
				proposer.giveItem(proposeeToTradeItem.getKey(), proposeeToTradeItem.getValue());
			}
		} else {
			TradeEntity proposeeEntity;
			int proposerId, proposeeId;

			proposerId = ((Individual) proposer).id.id;

			if (proposee instanceof Individual) {
				proposeeEntity = TradeEntity.INDIVIDUAL;
				proposeeId = ((Individual) proposee).id.id;
			} else {
				proposeeEntity = TradeEntity.PROP;
				proposeeId = ((ChestContainer) proposee).propId;
			}
			ClientServerInterface.transferItems(proposerItemsToTrade, proposerId, proposeeItemsToTrade, proposeeEntity, proposeeId);
		}
	}
}