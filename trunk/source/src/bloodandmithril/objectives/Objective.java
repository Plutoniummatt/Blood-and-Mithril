package bloodandmithril.objectives;

/**
 * An objective is a goal that can be completed.
 *
 * @author Matt
 */
public interface Objective {

	/**
	 * @return The current mission status
	 */
	public ObjectiveStatus getStatus();

	/**
	 * @return the worldId this Objective applies to, -1 if this field is meaningless
	 */
	public int getWorldId();

	/**
	 * Renders UI hints
	 */
	public void renderHints();

	/**
	 * The title of this {@link Objective}
	 */
	public String getTitle();


	public enum ObjectiveStatus {
		ACTIVE, COMPLETE, FAILED
	}
}