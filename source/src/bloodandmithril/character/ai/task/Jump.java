package bloodandmithril.character.ai.task;

import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.Domain.getIndividual;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.GroundTravellingIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;

import com.badlogic.gdx.math.Vector2;

/**
 * {@link AITask} for jumping
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Jump extends AITask {
	private static final long serialVersionUID = -3954194589753060040L;

	private final Vector2 from, to, jumpVector;
	private boolean jumped;

	/**
	 * Constructor
	 */
	public Jump(IndividualIdentifier hostId, Vector2 from, Vector2 to) {
		super(hostId);
		this.from = from;
		this.to = to;

		jumpVector = resolveJumpVector();
	}


	@Override
	public String getDescription() {
		return "Jumping";
	}


	@Override
	public boolean isComplete() {
		return getIndividual(hostId.getId()).getDistanceFrom(to) < 16f;
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	@Override
	public void execute(float delta) {
		Individual host = getIndividual(hostId.getId());
		if (obj(host.getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT)) {
			return;
		}

		if (jumped) {
			return;
		}

		if (host instanceof GroundTravellingIndividual) {
			host.clearCommands();
			((GroundTravellingIndividual) host).jump(jumpVector);
			jumped = true;
		}
	}


	private Vector2 resolveJumpVector() {
		return new Vector2(500, 500);
	}
}
