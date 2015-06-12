package bloodandmithril.objectives;

/**
 * An objective is a goal that can be completed.
 *
 * @author Matt
 */
public interface Objective {

	/**
	 * @return whether this objective is complete
	 */
	public boolean isComplete();

	/**
	 * @return whether this objective has failed and is an incompletable state
	 */
	public boolean hasFailed();

	/**
	 * @return the worldId this Objective applies to, -1 if this field is meaningless
	 */
	public int getWorldId();

	/**
	 * Renders UI hints
	 */
	public void renderHints();
}