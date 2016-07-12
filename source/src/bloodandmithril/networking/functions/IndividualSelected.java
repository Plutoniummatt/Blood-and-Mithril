package bloodandmithril.networking.functions;

import com.google.common.collect.Iterables;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.util.SerializableFunction;

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
	public IndividualSelected(final int individualId) {
		this.individualId = individualId;
	}


	@Override
	public Boolean call() {
		return Iterables.tryFind(Wiring.injector().getInstance(GameClientStateTracker.class).getSelectedIndividuals(), indi -> {
			return indi.getId().getId() == individualId;
		}).isPresent();
	}
}