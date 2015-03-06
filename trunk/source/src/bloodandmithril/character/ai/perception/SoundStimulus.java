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
	 * @return the position where this sound was emiited
	 */
	public Vector2 getEmissionPosition();

	/**
	 * Sets the emission position.
	 */
	public void setEmissionPosition(Vector2 position);
	
	@Override
	public default void stimulate(Individual individual) {
		if (individual instanceof Listener) {
			((Listener) individual).listen(this);
		}
	}
}