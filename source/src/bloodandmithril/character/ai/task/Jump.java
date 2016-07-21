package bloodandmithril.character.ai.task;

import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.Domain.getIndividual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.individuals.GroundTravellingIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.SerializableFunction;

/**
 * {@link AITask} for jumping
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Jump extends AITask {
	private static final long serialVersionUID = -3954194589753060040L;

	@Inject private transient UserInterface userInterface;

	private final SerializableFunction<Vector2> from;
	private final Vector2 to;
	private boolean jumped;

	/**
	 * Constructor
	 */
	public Jump(final IndividualIdentifier hostId, final SerializableFunction<Vector2> from, final Vector2 to) {
		super(hostId);
		this.from = from;
		this.to = to;
	}


	public Vector2 getDestination() {
		return to.cpy();
	}


	@Override
	public String getShortDescription() {
		return "Jumping";
	}


	@Override
	public boolean isComplete() {
		return jumped && !obj(getIndividual(hostId.getId()).getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT);
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	@Override
	protected void internalExecute(final float delta) {
		final Individual host = getIndividual(hostId.getId());
		if (obj(host.getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT)) {
			return;
		}

		if (jumped) {
			return;
		}

		if (host instanceof GroundTravellingIndividual) {
			if (host.getState().stamina < 0.1f) {
				userInterface.addFloatingText("Not enough stamina", Color.ORANGE, host.getEmissionPosition().cpy(), false, host.getWorldId());
				jumped = true;
				return;
			}

			host.clearCommands();
			((GroundTravellingIndividual) host).jump(resolveJumpVector());
			jumped = true;
		}
	}


	private Vector2 resolveJumpVector() {
		final Vector2 call = from.call();
		final Vector2 difference = to.cpy().sub(call);
		final Vector2 nor = difference.cpy().nor();
		if (nor.y < 0f) {
			return new Vector2();
		}
		return nor.scl(500f * Math.min(difference.len() / 75f, 1f));
	}
}
