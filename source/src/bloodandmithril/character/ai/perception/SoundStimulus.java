package bloodandmithril.character.ai.perception;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

import com.badlogic.gdx.math.Vector2;

/**
 * A stimulus that stimulates {@link Listener}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface SoundStimulus extends Stimulus {

	/**
	 * stimulates the listener
	 */
	public void stimulate(Listener listener, Vector2 position);

	/**
	 * @return the position where this sound was emiited
	 */
	public Vector2 getEmissionPosition();

	@Override
	public default void stimulate(Individual individual) {
		if (individual instanceof Listener) {
			stimulate((Listener) individual, getEmissionPosition());
		}
	}
}