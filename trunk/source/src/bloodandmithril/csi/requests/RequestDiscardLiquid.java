package bloodandmithril.csi.requests;

import bloodandmithril.character.ai.task.DiscardLiquid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.item.items.container.LiquidContainer;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.world.Domain;

/**
 * A {@link Request} for an individual to discard {@link Liquid} from a {@link LiquidContainer} into the world.
 * At world tile coordinates x and y.
 *
 * @author Matt
 */
public class RequestDiscardLiquid implements Request {

	private final int individualId;
	private final LiquidContainer container;
	private final float amount;
	private final int x;
	private final int y;

	/**
	 * Constructor
	 */
	public RequestDiscardLiquid(Individual individual, LiquidContainer container, float amount, int x, int y) {
		this.individualId = individual.getId().getId();
		this.container = container;
		this.amount = amount;
		this.x = x;
		this.y = y;
	}


	@Override
	public Responses respond() {
		Domain.getIndividuals().get(individualId).getAI().setCurrentTask(
			new DiscardLiquid(Domain.getIndividuals().get(individualId), x, y, container, amount)
		);

		Responses responses = new Responses(false);
		responses.add(new SyncFluidsNotification(Domain.getActiveWorld().getTopography().getFluids()));
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
}