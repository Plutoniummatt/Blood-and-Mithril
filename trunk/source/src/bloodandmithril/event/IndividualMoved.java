package bloodandmithril.event;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * An event that is fired when an {@link Individual} is manually moved.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class IndividualMoved extends Event {
	private static final long serialVersionUID = -3175949619548175761L;
	
	/**
	 * Constructor
	 */
	public IndividualMoved() {
		super();
	}
}
