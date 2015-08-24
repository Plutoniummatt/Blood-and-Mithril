package bloodandmithril.objectives.objective;

import bloodandmithril.event.Event;
import bloodandmithril.event.events.IndividualMoved;
import bloodandmithril.objectives.Objective;

/**
 * An {@link Objective} that is to just simply instruct any individual to move to a player specified location
 *
 * @author Matt
 */
public class MoveIndividualObjective implements Objective {
	private static final long serialVersionUID = -8400481940506283262L;

	private boolean individualMoved = false;

	/**
	 * Constructor
	 */
	public MoveIndividualObjective() {
	}


	@Override
	public ObjectiveStatus getStatus() {
		if (individualMoved) {
			return ObjectiveStatus.COMPLETE;
		} else {
			return ObjectiveStatus.ACTIVE;
		}
	}


	@Override
	public int getWorldId() {
		return -1;
	}


	@Override
	public void renderHints() {
	}


	@Override
	public String getTitle() {
		return "Move an individual";
	}


	@Override
	public void listen(Event event) {
		if (event instanceof IndividualMoved) {
			individualMoved = true;
		}
	}


	@Override
	public void uponCompletion() {
	}
}