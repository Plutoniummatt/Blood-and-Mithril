package bloodandmithril.util;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import bloodandmithril.core.Copyright;


/**
 * Binds a {@link Task} to the cursor.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class CursorBoundTask {

	protected JITTask task;
	private boolean isWorldCoordinate;

	/**
	 * Constructor
	 */
	public CursorBoundTask(JITTask task, boolean isWorldCoordinate) {
		this.task = task;
		this.isWorldCoordinate = isWorldCoordinate;
	}


	public void setTask(JITTask task) {
		this.task = task;
	}


	/**
	 * Executes
	 *
	 * @param x - the x coordinate of the mouse, could either be world or screen coords.
	 * @param y - the y coordinate of the mouse, could either be world or screen coords.
	 */
	public CursorBoundTask execute(int x, int y) {
		task.execute(x, y);
		return getImmediateTask();
	}


	/**
	 * Gets the {@link CursorBoundTask} to be executed after this one
	 */
	public abstract CursorBoundTask getImmediateTask();


	/**
	 * Renders any UI guides for this {@link CursorBoundTask}
	 */
	public abstract void renderUIGuide(SpriteBatch batch);


	/**
	 * @return whether this task relates to world coordinate
	 */
	public boolean isWorldCoordinate() {
		return isWorldCoordinate;
	}


	/**
	 * @return true if this can be executed
	 */
	public abstract boolean executionConditionMet();


	/**
	 * @return the UI help text that will be displayed next to the cursor when this {@link CursorBoundTask} is bound to the cursor.
	 */
	public abstract String getShortDescription();


	/**
	 * @return true if this task can be cancelled
	 */
	public abstract boolean canCancel();


	/**
	 * Called when key is pressed
	 */
	public abstract void keyPressed(int keyCode);
}