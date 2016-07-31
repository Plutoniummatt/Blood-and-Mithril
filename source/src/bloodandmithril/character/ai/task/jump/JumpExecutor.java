package bloodandmithril.character.ai.task.jump;

import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.Domain.getIndividual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.IndividualStateService;
import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.GroundTravellingIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.UserInterface;

/**
 * Executes a {@link Jump}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class JumpExecutor implements AITaskExecutor {
	private static float REQUIRED_STAMINA_TO_JUMP = 0.05f;

	@Inject private UserInterface userInterface;
	@Inject private IndividualStateService individualStateService;

	@Override
	public void execute(final AITask aiTask, final float delta) {
		final Jump jump = (Jump) aiTask;

		final Individual host = getIndividual(jump.getHostId().getId());
		if (obj(host.getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT)) {
			return;
		}

		if (jump.jumped) {
			return;
		}

		if (host instanceof GroundTravellingIndividual) {
			if (host.getState().stamina < REQUIRED_STAMINA_TO_JUMP) {
				userInterface.addFloatingText("Not enough stamina", Color.ORANGE, host.getEmissionPosition().cpy(), false, host.getWorldId());
				jump.jumped = true;
				return;
			}

			final Vector2 jumpVector = resolveJumpVector(jump);

			host.getState().velocity.x = jumpVector.x;
			host.getState().velocity.y = jumpVector.y;
			host.setCurrentAction(jumpVector.x < 0f ? Action.JUMP_LEFT : Action.JUMP_RIGHT);
			host.setAnimationTimer(0f);

			individualStateService.decreaseStamina(host, REQUIRED_STAMINA_TO_JUMP);

			jump.jumped = true;
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final Jump jump = (Jump) aiTask;

		return jump.jumped && !obj(getIndividual(jump.getHostId().getId()).getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT);
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		return false;
	}


	private Vector2 resolveJumpVector(final Jump jump) {
		final Vector2 call = jump.from.call();
		final Vector2 difference = jump.to.cpy().sub(call);
		final Vector2 nor = difference.cpy().nor();
		if (nor.y < 0f) {
			return new Vector2();
		}
		return nor.scl(500f * Math.min(difference.len() / 75f, 1f));
	}
}