package bloodandmithril.character.ai.routine.daily;

import java.util.ArrayDeque;
import java.util.Deque;

import bloodandmithril.character.ai.ExecutedBy;
import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.IndividualIdentifier;
import bloodandmithril.core.Copyright;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.window.EditAIRoutineWindow;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Epoch;

/**
 * {@link Routine} that executes at, or later than a specified time every day
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
@ExecutedBy(DailyRoutineExecutor.class)
public final class DailyRoutine extends Routine {
	private static final long serialVersionUID = -255141692263126217L;

	Epoch lastExecutedEpoch = null;
	Float routineTime;
	float toleranceTime;

	/**
	 * Constructor
	 */
	public DailyRoutine(final IndividualIdentifier hostId, final Float routineTime, final float toleranceTime) {
		super(hostId);
		this.routineTime = routineTime;
		this.toleranceTime = toleranceTime;
		setDescription("Daily routine");
	}


	@Override
	public final boolean areExecutionConditionsMet() {
		final Epoch currentEpoch = Domain.getWorld(getHost().getWorldId()).getEpoch();
		return currentEpoch.getTime() >= routineTime && currentEpoch.getTime() <= routineTime + toleranceTime && (lastExecutedEpoch == null || currentEpoch.dayOfMonth != lastExecutedEpoch.dayOfMonth);
	}


	public void setLastExecutedEpoch(final Epoch epoch) {
		this.lastExecutedEpoch = epoch.copy();
	}


	@Override
	public final Object getTaskGenerationParameter() {
		return getHost();
	}


	@Override
	public final Deque<Panel> constructEditWizard(final EditAIRoutineWindow parent) {
		final Deque<Panel> wizard = new ArrayDeque<>();
		wizard.add(new DailyRoutineInfoPanel(parent, this));
		return wizard;
	}
}