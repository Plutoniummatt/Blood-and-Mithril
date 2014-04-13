package bloodandmithril.csi.requests;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.Attack;
import bloodandmithril.csi.Request;
import bloodandmithril.csi.Response.Responses;
import bloodandmithril.world.Domain;

/**
 * {@link Request} for an {@link Individual} to attack another
 *
 * @author Matt
 */
public class SendAttackRequest implements Request {

	private final int attackerId;
	private final int victimId;

	/** Constructor */
	public SendAttackRequest(int attackerId, int victimId) {
		this.attackerId = attackerId;
		this.victimId = victimId;
	}


	@Override
	public Responses respond() {
		Domain.getIndividuals().get(attackerId).getAI().setCurrentTask(
			new Attack(
				Domain.getIndividuals().get(attackerId),
				Domain.getIndividuals().get(victimId)
			)
		);

		Responses responses = new Responses(false);
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