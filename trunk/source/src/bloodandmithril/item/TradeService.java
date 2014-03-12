package bloodandmithril.item;

import java.util.HashMap;
import java.util.Map.Entry;

import bloodandmithril.character.Individual;
import bloodandmithril.character.skill.Skills;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.prop.building.ConstructionWithContainer.ConstructionContainer;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.world.Domain;

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
			proposerActualValue = proposerActualValue + entry.getValue() * entry.getKey().value;
		}

		for (Entry<Item, Integer> entry : forThis.entrySet()) {
			proposeeActualValue = proposeeActualValue + entry.getValue() * entry.getKey().value;
		}

		float proposerEffectiveValue = ((float)proposer.getSkills().getTrading()/(float)Skills.MAX_LEVEL + 1f)/2f * proposerActualValue;
		float proposeeEffectiveValue = ((float)proposee.getSkills().getTrading()/(float)Skills.MAX_LEVEL + 1f)/2f * proposeeActualValue;

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
				if (proposee instanceof ConstructionContainer && Domain.getProps().get(((ConstructionContainer) proposee).propId) instanceof Furnace) {
					Furnace furnace = (Furnace) Domain.getProps().get(((ConstructionContainer) proposee).propId);

					if (proposerToTradeItem.getKey() instanceof Fuel) {
						Fuel fuel = (Fuel) proposerToTradeItem.getKey();
						furnace.setCombustionDurationRemaining(
							furnace.getCombustionDurationRemaining() + fuel.getCombustionDuration() * (Furnace.MIN_TEMP / furnace.getTemperature()) * proposerToTradeItem.getValue()
						);
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
				proposeeId = ((ConstructionContainer) proposee).propId;
			}
			ClientServerInterface.SendRequest.sendTransferItemsRequest(proposerItemsToTrade, proposerId, proposeeItemsToTrade, proposeeEntity, proposeeId);
		}
	}
}