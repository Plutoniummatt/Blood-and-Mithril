package bloodandmithril.character.ai.perception;

import java.io.Serializable;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;

/**
 * Stimuli stimulate {@link Individual}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface Stimulus extends Serializable {

	/**
	 * Stimulates an individual
	 */
	public void stimulate(Individual individual);
}