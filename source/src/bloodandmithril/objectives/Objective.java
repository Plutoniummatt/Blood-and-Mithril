package bloodandmithril.objectives;

import bloodandmithril.event.EventListener;


/**
 * An objective is a goal that can be completed.
 *
 * @author Matt
 */
public interface Objective extends EventListener {

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
		ACTIVE("Active"), COMPLETE("Complete"), FAILED("Failed");

		private ObjectiveStatus(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		private String description;
	}


	/**
	 * Called when this objective is complete
	 */
	public void uponCompletion();
}