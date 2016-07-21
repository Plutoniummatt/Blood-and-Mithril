package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.util.CursorBoundTask;
import bloodandmithril.util.JITTask;

/**
 * A {@link CursorBoundTask} that selects an area
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class ChooseAreaCursorBoundTask extends CursorBoundTask {

	@Inject private UserInterface userInterface;

	private Vector2 start, finish;

	/**
	 * Constructor
	 */
	public ChooseAreaCursorBoundTask(final JITTask task, final boolean isWorldCoordinate) {
		super(task, isWorldCoordinate);
	}


	@Override
	public CursorBoundTask execute(final int x, final int y) {
		if (start == null) {
			start = new Vector2(getMouseWorldX(), getMouseWorldY());
			return this;
		} else if (finish == null) {
			finish = new Vector2(getMouseWorldX(), getMouseWorldY());
		}

		task.execute(start, finish);
		return getImmediateTask();
	}


	@Override
	public void renderUIGuide(final Graphics graphics) {
		if (start == null) {
			return;
		} else {
			Gdx.gl20.glLineWidth(2f);
			userInterface.getShapeRenderer().setColor(
				executionConditionMet() ? 0f : 1f,
				executionConditionMet() ? 1f : 0f,
				0f,
				0.8f
			);
			userInterface.getShapeRenderer().begin(ShapeType.Line);
			userInterface.getShapeRenderer().rect(
				worldToScreenX(start.x),
				worldToScreenY(start.y),
				getMouseWorldX() - start.x,
				getMouseWorldY() - start.y
			);
			userInterface.getShapeRenderer().end();
		}
	}
}