package bloodandmithril.character.ai;

import java.util.Deque;

import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;

/**
 * A player customisable {@link AITask}, designed for automation and eliminating the need of micro-managing laborious tasks..
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public abstract class Routine<C> extends AITask {
	private static final long serialVersionUID = -8502601311459390398L;
	private int priority = 1;
	private String description = "";

	/**
	 * Protected constructor
	 */
	public Routine(IndividualIdentifier hostId) {
		super(hostId);
	}


	public int getPriority() {
		return priority;
	}


	public void setPriority(int priority) {
		this.priority = priority;
	}


	@Override
	public String getShortDescription() {
		return description;
	}


	/**
	 * Sets the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return whether or not this {@link Routine} meets execution conditions
	 */
	public abstract boolean areExecutionConditionsMet();


	/**
	 * Prepares the routine for execution
	 */
	public abstract void prepare();


	/**
	 * @param aiTaskGenerator the task generator to set
	 */
	public abstract void setAiTaskGenerator(TaskGenerator<C> aiTaskGenerator);


	/**
	 * @return the UI wizard for editing this {@link Routine}
	 */
	public abstract Deque<Panel> constructEditWizard(EditAIRoutineWindow parent);
}