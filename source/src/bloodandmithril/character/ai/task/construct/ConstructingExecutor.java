package bloodandmithril.character.ai.task.construct;

import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.construct.Construct.Constructing;
import bloodandmithril.core.Copyright;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.world.Domain;

/**
 * Executes {@link Constructing}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ConstructingExecutor implements AITaskExecutor {


	@Override
	public void execute(final AITask aiTask, final float delta) {
		final Constructing task = (Constructing) aiTask;

		final Construction construction = (Construction) Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.constructionId);
		if (construction != null) {
			if (task.deconstruct) {
				construction.deconstruct(Domain.getIndividual(task.getHostId().getId()), delta);
			} else {
				construction.construct(Domain.getIndividual(task.getHostId().getId()), delta);
			}
		} else {
			task.stop = true;
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		final Constructing task = (Constructing) aiTask;
		if (task.deconstruct) {
			return !Domain.getWorld(task.getHost().getWorldId()).props().hasProp(task.constructionId) || task.stop || !((Construction) Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.constructionId)).canDeconstruct();
		} else {
			return ((Construction) Domain.getWorld(task.getHost().getWorldId()).props().getProp(task.constructionId)).getConstructionProgress() == 1f || task.stop;
		}
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		return false;
	}
}
