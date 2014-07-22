package bloodandmithril.util;

import bloodandmithril.core.Copyright;


/**
 * Binds a {@link Task} to the cursor.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
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


	/**
	 * Renders any UI guides for this {@link CursorBoundTask}
	 */
	public abstract void renderUIGuide();


	public boolean isWorldCoordinate() {
		return isWorldCoordinate;
	}


	/**
	 * @return the UI help text that will be displayed next to the cursor when this {@link CursorBoundTask} is bound to the cursor.
	 */
	public abstract String getShortDescription();
}