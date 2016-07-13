package bloodandmithril.control.rightclick;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.control.BloodAndMithrilClientInputProcessor;
import bloodandmithril.control.RightClickHandler;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.CursorBoundTask;

/**
 * Cancels {@link CursorBoundTask}s
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CancelCursorBoundTaskRightClickHandler implements RightClickHandler {

	@Inject
	private BloodAndMithrilClientInputProcessor inputProcessor;

	@Override
	public boolean rightClick(final boolean doubleClick) {
		final CursorBoundTask cursorBoundTask = inputProcessor.getCursorBoundTask();
		if (cursorBoundTask != null && cursorBoundTask.canCancel()) {
			inputProcessor.setCursorBoundTask(null);
			return true;
		}

		return false;
	}
}