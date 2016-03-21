package bloodandmithril.util.cursorboundtask;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static bloodandmithril.control.InputUtilities.worldToScreenX;
import static bloodandmithril.control.InputUtilities.worldToScreenY;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
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

	private Vector2 start, finish;

	/**
	 * Constructor
	 */
	public ChooseAreaCursorBoundTask(JITTask task, boolean isWorldCoordinate) {
		super(task, isWorldCoordinate);
	}


	@Override
	public CursorBoundTask execute(int x, int y) {
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
	public void renderUIGuide(SpriteBatch batch) {
		if (start == null) {
			return;
		} else {
			Gdx.gl20.glLineWidth(2f);
			UserInterface.shapeRenderer.setColor(
				executionConditionMet() ? 0f : 1f,
				executionConditionMet() ? 1f : 0f,
				0f,
				0.8f
			);
			UserInterface.shapeRenderer.begin(ShapeType.Line);
			UserInterface.shapeRenderer.rect(
				worldToScreenX(start.x),
				worldToScreenY(start.y),
				getMouseWorldX() - start.x,
				getMouseWorldY() - start.y
			);
			UserInterface.shapeRenderer.end();
		}
	}
}