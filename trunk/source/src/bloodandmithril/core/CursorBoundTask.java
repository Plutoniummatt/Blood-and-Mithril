package bloodandmithril.core;

import bloodandmithril.util.JITTask;
import bloodandmithril.util.Task;

/**
 * Binds a {@link Task} to the cursor.
 *
 * @author Matt
 */
public abstract class CursorBoundTask {

	private JITTask task;
	private boolean isWorldCoordinate;

	/**
	 * Constructor
	 */
	public CursorBoundTask(JITTask task, boolean isWorldCoordinate) {
		this.task = task;
		this.isWorldCoordinate = isWorldCoordinate;
	}


	/**
	 * Executes
	 *
	 * @param x - the x coordinate of the mouse, could either be world or screen coords.
	 * @param y - the y coordinate of the mouse, could either be world or screen coords.
	 */
	public void execute(int x, int y) {
		task.execute(x, y);
	}


	public boolean isWorldCoordinate() {
		return isWorldCoordinate;
	}


	/**
	 * @return the UI help text that will be displayed next to the cursor when this {@link CursorBoundTask} is bound to the cursor.
	 */
	public abstract String getShortDescription();
}