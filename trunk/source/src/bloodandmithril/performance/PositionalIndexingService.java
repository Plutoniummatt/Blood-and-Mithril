package bloodandmithril.performance;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;

/**
 * Service to re-index indexable entities.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class PositionalIndexingService {

	/**
	 * Clears down all positional indexes and reindexes all indexed entities
	 */
	public static void reindex() {
		for (World world : Domain.getWorlds().values()) {
			for (PositionalIndexNode node : world.getPositionalIndexMap().getAllNodes()) {
				node.clear();
			}

			for (int individualId : world.getIndividuals()) {
				indexInvidivual(Domain.getIndividual(individualId));
			}

			for (Item item : world.items().getItems()) {
				item.updatePositionalIndex();
			}

			for (Prop prop : world.props().getProps()) {
				prop.updatePositionIndex();
			}
		}
	}


	public static void indexInvidivual(Individual indi) {
		for (PositionalIndexNode node : Domain.getWorld(indi.getWorldId()).getPositionalIndexMap().getNearbyNodes(indi.getState().position.x, indi.getState().position.y)) {
			node.removeIndividual(indi.getId().getId());
		}

		Domain.getWorld(indi.getWorldId()).getPositionalIndexMap().get(indi.getState().position.x, indi.getState().position.y).addIndividual(indi.getId().getId());
	}
}