package bloodandmithril.item;

import java.util.HashMap;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.prop.Prop;

/**
 * Evaluates a trade proposal.
 *
 * @author Matt
 */
public class TradeService {

	/**
	 * Evaluates a trade proposal
	 */
	public static boolean evaluate(Individual proposer, HashMap<Item, Integer> tradeThis, Individual proposee, HashMap<Item, Integer> forThis) {

		if (proposee.isControllable()) {
			return true;
		}

		float proposerActualValue = 0, proposeeActualValue = 0;

		for (Entry<Item, Integer> entry : tradeThis.entrySet()) {
			proposerActualValue = proposerActualValue + entry.getValue() * entry.getKey().getValue();
		}

		for (Entry<Item, Integer> entry : forThis.entrySet()) {
			proposeeActualValue = proposeeActualValue + entry.getValue() * entry.getKey().getValue();
		}

		// At max level, proposer effective value is 100%
		// At min level, proposer effective value is 50%
		// Example:
		// Level 56 Trader trading 500g worth of items with
		// Level ?? Trader for 400g worth of items
		// ---------
		// 56/100 * 500g = 280g proposerEffective
		// Proposee will not accept this trade, as he is **effectively** losing out, even if in reality he is winning.
		// ---------
		// The minimum trade skill required for the trade to work, would be the lowest skill level that makes the effective
		// value greater or equal to 400g, in this case, 80.
		float proposerEffectiveValue = (Skills.getRatioToMax(proposer.getSkills().getTrading()) + 1f)/2f * proposerActualValue;
		float proposeeEffectiveValue = proposeeActualValue;

		if (proposerEffectiveValue > proposeeEffectiveValue) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * The trade proposal was accepted by the proposee, this method transfers the {@link Item}s and finalizes the trade
	 */
	public synchronized static void transferItems(HashMap<Item, Integer> proposerItemsToTrade, Container proposer, HashMap<Item, Integer> proposeeItemsToTrade, Container proposee) {
		if (ClientServerInterface.isServer()) {
			for (Entry<Item, Integer> proposerToTradeItem : proposerItemsToTrade.entrySet()) {
				for (int i = proposerToTradeItem.getValue(); i > 0; i--) {
					if (proposer.takeItem(proposerToTradeItem.getKey()) == 1) {
						proposee.giveItem(proposerToTradeItem.getKey());
					}
				}
			}
			for (Entry<Item, Integer> proposeeToTradeItem : proposeeItemsToTrade.entrySet()) {
				for (int i = proposeeToTradeItem.getValue(); i > 0; i--) {
					if (proposee.takeItem(proposeeToTradeItem.getKey()) == 1) {
						proposer.giveItem(proposeeToTradeItem.getKey());
					}
				}
			}
		} else {
			TradeEntity proposeeEntity;
			int proposerId, proposeeId;

			proposerId = ((Individual) proposer).getId().getId();

			if (proposee instanceof Individual) {
				proposeeEntity = TradeEntity.INDIVIDUAL;
				proposeeId = ((Individual) proposee).getId().getId();
			} else {
				proposeeEntity = TradeEntity.PROP;
				proposeeId = ((Prop) proposee).id;
			}
			ClientServerInterface.SendRequest.sendTransferItemsRequest(proposerItemsToTrade, proposerId, proposeeItemsToTrade, proposeeEntity, proposeeId);
		}
	}
}