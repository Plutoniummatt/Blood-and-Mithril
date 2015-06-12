package bloodandmithril.objectives.objective.function;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;

/**
 * Any controllable individual will return true
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class AnyControllableIndividual extends SerializableMappingFunction<Individual, Boolean> {
	private static final long serialVersionUID = 4256915557653497560L;

	@Override
	public Boolean apply(Individual input) {
		return input.isControllable();
	}
}
