package bloodandmithril.item;

import java.util.HashMap;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.faction.FactionControlService;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.Proficiency;
import bloodandmithril.character.proficiency.proficiencies.Trading;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.networking.functions.IndividualSelected;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.ui.UserInterface;

/**
 * Evaluates a trade proposal.
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class TradeService {

	@Inject private FactionControlService factionControlService;
	@Inject private UserInterface userInterface;

	/**
	 * Evaluates a trade proposal
	 */
	public boolean evaluate(final Individual proposer, final HashMap<Item, Integer> tradeThis, final Individual proposee, final HashMap<Item, Integer> forThis) {

		if (factionControlService.isControllable(proposee) || !proposee.isAlive()) {
			return true;
		}

		float proposerActualValue = 0, proposeeActualValue = 0;

		for (final Entry<Item, Integer> entry : tradeThis.entrySet()) {
			proposerActualValue = proposerActualValue + entry.getValue() * entry.getKey().getValue();
		}

		for (final Entry<Item, Integer> entry : forThis.entrySet()) {
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
		final float radioToMax = Proficiency.getRatioToMax(proposer.getProficiencies().getProficiency(Trading.class).getLevel());
		final float proposerEffectiveValue = (radioToMax  + 1f) /2f * proposerActualValue;
		final float proposeeEffectiveValue = proposeeActualValue;

		if (proposerEffectiveValue >= proposeeEffectiveValue) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * The trade proposal was accepted by the proposee, this method transfers the {@link Item}s and finalizes the trade
	 */
	public synchronized void transferItems(final HashMap<Item, Integer> proposerItemsToTrade, final Container proposer, final HashMap<Item, Integer> proposeeItemsToTrade, final Container proposee) {

		final float proposerItemsToTradeMass = (float) proposerItemsToTrade.entrySet().stream().mapToDouble(entry -> {
			return entry.getKey().getMass() * entry.getValue();
		}).sum();
		final float proposeeItemsToTradeMass = (float) proposeeItemsToTrade.entrySet().stream().mapToDouble(entry -> {
			return entry.getKey().getMass() * entry.getValue();
		}).sum();
		final int proposerItemsToTradeVolume = proposerItemsToTrade.entrySet().stream().mapToInt(entry -> {
			return entry.getKey().getVolume() * entry.getValue();
		}).sum();
		final int proposeeItemsToTradeVolume = proposeeItemsToTrade.entrySet().stream().mapToInt(entry -> {
			return entry.getKey().getVolume() * entry.getValue();
		}).sum();

		final boolean proposerOverWeightLimitPostTrade = proposer.getCurrentLoad() - proposerItemsToTradeMass + proposeeItemsToTradeMass > proposer.getMaxCapacity() && proposer.getWeightLimited();
		final boolean proposeeOverWeightLimitPostTrade = proposee.getCurrentLoad() + proposerItemsToTradeMass - proposeeItemsToTradeMass > proposee.getMaxCapacity() && proposee.getWeightLimited();

		final boolean proposerWeightIncreasing = proposeeItemsToTradeMass - proposerItemsToTradeMass > 0;
		final boolean proposeeWeightIncreasing = proposerItemsToTradeMass - proposeeItemsToTradeMass > 0;

		final boolean proposerOverVolumeLimitPostTrade = proposer.getCurrentVolume() - proposerItemsToTradeVolume + proposeeItemsToTradeVolume > proposer.getMaxVolume();
		final boolean proposeeOverVolumeLimitPostTrade = proposee.getCurrentVolume() + proposerItemsToTradeVolume - proposeeItemsToTradeVolume > proposee.getMaxVolume();

		final boolean proposerVolumeIncreasing = proposeeItemsToTradeVolume - proposerItemsToTradeVolume > 0;
		final boolean proposeeVolumeIncreasing = proposerItemsToTradeVolume - proposeeItemsToTradeVolume > 0;

		if (
			proposerOverWeightLimitPostTrade && proposerWeightIncreasing ||
			proposeeOverWeightLimitPostTrade && proposeeWeightIncreasing ||
			proposerOverVolumeLimitPostTrade && proposerVolumeIncreasing ||
			proposeeOverVolumeLimitPostTrade && proposeeVolumeIncreasing
		) {
			userInterface.addGlobalMessage("Can not trade", "One or more parties do not have enough inventory space.", new IndividualSelected(((Individual) proposer).getId().getId()));
			return;
		}

		if (ClientServerInterface.isServer()) {
			for (final Entry<Item, Integer> proposerToTradeItem : proposerItemsToTrade.entrySet()) {
				for (int i = proposerToTradeItem.getValue(); i > 0; i--) {
					if (proposer.takeItem(proposerToTradeItem.getKey()) == 1) {
						proposee.giveItem(proposerToTradeItem.getKey());
					}
				}
			}
			for (final Entry<Item, Integer> proposeeToTradeItem : proposeeItemsToTrade.entrySet()) {
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