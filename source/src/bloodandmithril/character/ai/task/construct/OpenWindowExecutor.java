package bloodandmithril.character.ai.task.construct;

import static bloodandmithril.networking.ClientServerInterface.isClient;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.AITaskExecutor;
import bloodandmithril.character.ai.task.construct.ConstructDeconstruct.OpenWindow;
import bloodandmithril.core.Copyright;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.ConstructionWindow;

/**
 * Executes {@link OpenWindow}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class OpenWindowExecutor implements AITaskExecutor {

	@Inject private UserInterface userInterface;

	@Override
	public void execute(final AITask aiTask, final float delta) {
		final OpenWindow task = (OpenWindow) aiTask;

		if (task.getHost().getInteractionBox().isWithinBox(task.getParent().construction.position)) {
			if (isClient()) {
				userInterface.addLayeredComponentUnique(
					new ConstructionWindow(
						task.getHost().getId().getSimpleName() + " interacting with " + task.getParent().construction.getTitle(),
						true,
						task.getHost(),
						task.getParent().construction
					)
				);
			} else {
				ClientServerInterface.SendNotification.notifyConstructionWindowOpen(task.getHost().getId().getId(), task.getParent().construction.id, task.getParent().connectionId);
			}
			task.opened = true;
		}
	}


	@Override
	public boolean isComplete(final AITask aiTask) {
		return ((OpenWindow) aiTask).opened;
	}


	@Override
	public boolean uponCompletion(final AITask aiTask) {
		return false;
	}
}