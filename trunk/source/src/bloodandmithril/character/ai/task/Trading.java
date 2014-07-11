package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.csi.requests.TransferItems.TradeEntity;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;

/**
 * An {@link AITask} which indicates an {@link Individual} is trading
 *
 * @author Matt
 */
public class Trading extends AITask {
	private static final long serialVersionUID = 6325569855563214762L;

	private final Individual proposer;
	private final Container proposee;
	private Prop prop;
	private final TradeEntity entity;

	/**
	 * Constructor
	 */
	public Trading(IndividualIdentifier hostId, int otherId, TradeEntity entity) {
		super(hostId);
		this.entity = entity;
		this.proposer = Domain.getIndividuals().get(hostId.getId());

		if (entity == TradeEntity.INDIVIDUAL) {
			this.proposee = Domain.getIndividuals().get(otherId);
		} else {
			prop = Domain.getProps().get(otherId);
			this.proposee = (Container) prop;
		}
	}


	@Override
	public String getDescription() {
		return "Trading";
	}


	@Override
	public boolean isComplete() {
		if (entity == TradeEntity.INDIVIDUAL) {
			return proposer.getState().position.cpy().sub(((Individual) proposee).getState().position.cpy()).len() > 64;
		} else {
			return proposer.getState().position.cpy().sub(prop.position.cpy()).len() > 64;
		}
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	@Override
	public void execute(float delta) {
	}
}