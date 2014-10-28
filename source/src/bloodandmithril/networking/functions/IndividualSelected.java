package bloodandmithril.networking.functions;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.world.Domain;

import com.google.common.collect.Iterables;

/**
 * {@link SerializableFunction} to determine is an {@link Individual} is selected
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class IndividualSelected implements SerializableFunction<Boolean> {
	private static final long serialVersionUID = -7026957853672188413L;
	private int individualId;
	
	/**
	 * Constructor
	 */
	public IndividualSelected(int individualId) {
		this.individualId = individualId;
	}

	
	@Override
	public Boolean call() {
		return Iterables.tryFind(Domain.getSelectedIndividuals(), indi -> {
			return indi.getId().getId() == individualId;
		}).isPresent();
	}
}