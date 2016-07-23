package bloodandmithril.character.ai.task.jump;

import static bloodandmithril.util.ComparisonUtil.obj;
import static bloodandmithril.world.Domain.getIndividual;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.individuals.GroundTravellingIndividual;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
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
	
	@Inject private UserInterface userInterface;

	@Override
	public void execute(AITask aiTask, float delta) {
		Jump jump = (Jump) aiTask;
		
		final Individual host = getIndividual(jump.getHostId().getId());
		if (obj(host.getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT)) {
			return;
		}

		if (jump.jumped) {
			return;
		}

		if (host instanceof GroundTravellingIndividual) {
			if (host.getState().stamina < 0.1f) {
				userInterface.addFloatingText("Not enough stamina", Color.ORANGE, host.getEmissionPosition().cpy(), false, host.getWorldId());
				jump.jumped = true;
				return;
			}

			host.clearCommands();
			((GroundTravellingIndividual) host).jump(resolveJumpVector(jump));
			jump.jumped = true;
		}		
	}
	

	@Override
	public boolean isComplete(AITask aiTask) {
		Jump jump = (Jump) aiTask;
		
		return jump.jumped && !obj(getIndividual(jump.getHostId().getId()).getCurrentAction()).oneOf(Action.JUMP_LEFT, Action.JUMP_RIGHT);
	}
	

	@Override
	public boolean uponCompletion(AITask aiTask) {
		return false;
	}

	
	private Vector2 resolveJumpVector(Jump jump) {
		final Vector2 call = jump.from.call();
		final Vector2 difference = jump.to.cpy().sub(call);
		final Vector2 nor = difference.cpy().nor();
		if (nor.y < 0f) {
			return new Vector2();
		}
		return nor.scl(500f * Math.min(difference.len() / 75f, 1f));
	}
}