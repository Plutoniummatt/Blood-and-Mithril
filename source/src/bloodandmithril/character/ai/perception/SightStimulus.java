package bloodandmithril.character.ai.perception;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

import com.badlogic.gdx.math.Vector2;

/**
 * A stimulus that stimulates {@link Observer}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface SightStimulus extends Stimulus {

	/**
	 * @return the position where this stimulus was sighted
	 */
	public Vector2 getSightLocation();

	@Override
	public default void stimulate(Individual individual) {
		if (individual instanceof Observer) {
			((Observer) individual).reactToSightStimulus(this);
		}
	}
}