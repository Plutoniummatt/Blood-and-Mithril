package bloodandmithril.character.ai.routine;

import java.io.Serializable;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.core.Copyright;

/**
 * A condition, used to determine if a {@link Routine} should be executed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Condition extends Serializable {

	/**
	 * @return whether or not the condition has been met
	 */
	public boolean met();
}