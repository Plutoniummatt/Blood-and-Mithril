package bloodandmithril.character.ai.task;

import bloodandmithril.character.ai.AITask;
import bloodandmithril.character.ai.RoutineTask;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.DailyRoutine;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine;
import bloodandmithril.character.ai.routine.IndividualConditionRoutine;
import bloodandmithril.character.ai.routine.StimulusDrivenRoutine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Name;
import bloodandmithril.ui.components.ContextMenu;

import com.google.inject.Inject;

/**
 * Instructs {@link Individual} to speak
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@Name(name = "Speak")
public class Speak extends AITask implements RoutineTask {
	private static final long serialVersionUID = -5210580892146755047L;
	private String text;
	private long duration;
	private boolean spoken = false;

	@Inject
	Speak() {
		super(null);
	}

	/**
	 * @param Constructor
	 */
	public Speak(Individual host, String text, long duration) {
		super(host.getId());
		this.text = text;
		this.duration = duration;
	}


	@Override
	public String getShortDescription() {
		return "Speaking";
	}


	@Override
	public boolean isComplete() {
		return spoken;
	}


	@Override
	public boolean uponCompletion() {
		return false;
	}


	@Override
	public void execute(float delta) {
		if (!spoken) {
			getHost().speak(text, duration);
			spoken = true;
		}
	}


	@Override
	public String getDetailedDescription() {
		return getHost().getId().getSimpleName() + " says \"" + text + "\"";
	}


	@Override
	public ContextMenu getDailyRoutineContextMenu(Individual host, DailyRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getEntityVisibleRoutineContextMenu(Individual host, EntityVisibleRoutine<? extends Visible> routine) {
		return null;
	}


	@Override
	public ContextMenu getIndividualConditionRoutineContextMenu(Individual host, IndividualConditionRoutine routine) {
		return null;
	}


	@Override
	public ContextMenu getStimulusDrivenRoutineContextMenu(Individual host, StimulusDrivenRoutine routine) {
		return null;
	}
}