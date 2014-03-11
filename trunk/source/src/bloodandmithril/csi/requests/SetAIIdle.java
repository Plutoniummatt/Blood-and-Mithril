package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.task.Idle;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * Sends a {@link Request} to make an {@link Individual}'s {@link AITask} {@link Idle}
 *
 * @author Matt
 */
public class SetAIIdle implements Request {

	private final int individualId;

	/**
	 * Constructor
	 */
	public SetAIIdle(int individualId) {
		this.individualId = individualId;
	}


	@Override
	public Responses respond() {
		Responses responses = new Responses(false);
		Individual individual = Domain.getIndividuals().get(individualId);
		if (individual.getSelectedByClient().isEmpty()) {
			individual.getAI().setToAuto(true);
		} else {
			individual.getAI().setCurrentTask(new Idle());
		}
		return responses;
	}


	@Override
	public boolean tcp() {
		return false;
	}


	@Override
	public boolean notifyOthers() {
		return false;
	}
}
