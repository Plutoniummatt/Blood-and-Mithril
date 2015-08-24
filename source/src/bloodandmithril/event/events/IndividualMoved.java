package bloodandmithril.event.events;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.event.Event;

/**
 * An event that is fired when an {@link Individual} is manually moved.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualMoved extends Event {
	private static final long serialVersionUID = -3175949619548175761L;
	private final int individualId;

	/**
	 * Constructor
	 */
	public IndividualMoved(Individual individual) {
		super();

		this.individualId = individual.getId().getId();
	}

	public int getIndividualId() {
		return individualId;
	}
}
