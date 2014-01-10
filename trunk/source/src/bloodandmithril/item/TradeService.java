package bloodandmithril.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.CSITrade.TradeEntity;
import bloodandmithril.prop.building.Chest.ChestContainer;
import bloodandmithril.ui.components.panel.ScrollableListingPanel.ListingMenuItem;

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
	public synchronized static void transferItems(HashMap<ListingMenuItem<Item>, Integer> proposerItemsToTrade, Container proposer, HashMap<ListingMenuItem<Item>, Integer> proposeeItemsToTrade, Container proposee) {
		if ("true".equals(System.getProperty("server"))) {
			for (Entry<ListingMenuItem<Item>, Integer> proposerToTradeItem : proposerItemsToTrade.entrySet()) {
				proposer.takeItem(proposerToTradeItem.getKey().t, proposerToTradeItem.getValue());
				proposee.giveItem(proposerToTradeItem.getKey().t, proposerToTradeItem.getValue());
			}
			for (Entry<ListingMenuItem<Item>, Integer> proposeeToTradeItem : proposeeItemsToTrade.entrySet()) {
				proposee.takeItem(proposeeToTradeItem.getKey().t, proposeeToTradeItem.getValue());
				proposer.giveItem(proposeeToTradeItem.getKey().t, proposeeToTradeItem.getValue());
			}
		} else {
			TradeEntity proposerEntity, proposeeEntity;
			int proposerId, proposeeId;

			if (proposer instanceof Individual) {
				proposerEntity = TradeEntity.INDIVIDUAL;
				proposerId = ((Individual) proposer).id.id;
			} else {
				proposerEntity = TradeEntity.PROP;
				proposerId = ((ChestContainer) proposer).propId;
			}

			if (proposee instanceof Individual) {
				proposeeEntity = TradeEntity.INDIVIDUAL;
				proposeeId = ((Individual) proposer).id.id;
			} else {
				proposeeEntity = TradeEntity.PROP;
				proposeeId = ((ChestContainer) proposer).propId;
			}
			ClientServerInterface.trade(proposerItemsToTrade, proposerEntity, proposerId, proposeeItemsToTrade, proposeeEntity, proposeeId);
		}
	}
}