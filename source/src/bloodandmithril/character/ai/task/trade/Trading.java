package bloodandmithril.character.ai.task.trade;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.networking.requests.TransferItems.TradeEntity;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * An {@link AITask} which indicates an {@link Individual} is trading
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@ExecutedBy(TradingExecutor.class)
public class Trading extends AITask {
	private static final long serialVersionUID = 6325569855563214762L;

	final Individual proposer;
	final Container proposee;
	Prop prop;
	final TradeEntity entity;

	/**
	 * Constructor
	 */
	public Trading(final IndividualIdentifier hostId, final int otherId, final TradeEntity entity) {
		super(hostId);
		this.entity = entity;
		this.proposer = Domain.getIndividual(hostId.getId());

		if (entity == TradeEntity.INDIVIDUAL) {
			this.proposee = Domain.getIndividual(otherId);
		} else {
			prop = Domain.getWorld(proposer.getWorldId()).props().getProp(otherId);
			this.proposee = (Container) prop;
		}
	}


	@Override
	public String getShortDescription() {
		return "Trading";
	}
}