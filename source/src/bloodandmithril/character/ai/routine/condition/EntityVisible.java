package bloodandmithril.character.ai.routine.condition;

import java.util.List;

import bloodandmithril.character.ai.perception.Observer;
import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.Condition;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;

/**
 * A {@link Condition} that tests visibility of a {@link Visible} entity.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class EntityVisible implements Condition {
	private static final long serialVersionUID = 3332283367475958938L;

	private SerializableMappingFunction<Visible, Boolean> identificationFunction;
	private int observerId;
	private Class<?> observerClass;

	/**
	 * Constructor
	 */
	public EntityVisible(SerializableMappingFunction<Visible, Boolean> identificationFunction, Observer observer) {
		this.identificationFunction = identificationFunction;
		if (observer instanceof Individual) {
			this.observerId = ((Individual) observer).getId().getId();
			this.observerClass = Individual.class;
		}
	}


	@Override
	public boolean met() {
		if (observerClass.equals(Individual.class)) {
			Individual individual = Domain.getIndividual(observerId);
			List<Visible> observed = ((Observer) individual).observe(individual.getWorldId(), individual.getId().getId());
			for (Visible v : observed) {
				if (identificationFunction.apply(v)) {
					return true;
				}
			}
		}

		return false;
	}
}