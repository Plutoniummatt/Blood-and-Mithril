package bloodandmithril.ui.components.window;

import java.util.Comparator;
import java.util.Map;

import bloodandmithril.character.ai.Routine;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.Domain;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

/**
 * {@link Window} for displaying and editting AI {@link Routine}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class AIRoutinesWindow extends ScrollableListingWindow<Routine, String> {

	private static Function<Routine, String> fn = new Function<Routine, String>() {
		@Override
		public String apply(Routine r) {
			return r.getDescription();
		}
	};

	private static Comparator<Routine> routineComparator = new Comparator<Routine>() {
		@Override
		public int compare(Routine o1, Routine o2) {
			return new Integer(o1.getPriority()).compareTo(o2.getPriority());
		}
	};

	private int individualId;

	/**
	 * Construction
	 */
	public AIRoutinesWindow(
		Individual individual
	) {
		super(400, 500, "AI Routines for " + individual.getId().getSimpleName(), true, 400, 500, true, true,
			buildMap(individual),
			fn,
			routineComparator
		);
		this.individualId = individual.getId().getId();
	}


	private static Map<Routine, String> buildMap(Individual individual) {
		Map<Routine, String> map = Maps.newHashMap();
		for (Routine r : individual.getAI().getAiRoutines()) {
			map.put(r, r.getDescription());
		}
		return map;
	}


	@Override
	public void refresh() {
		buildListing(
			buildMap(Domain.getIndividual(individualId)),
			routineComparator
		);
	}


	@Override
	public Object getUniqueIdentifier() {
		return getClass().getSimpleName() + individualId;
	}
}