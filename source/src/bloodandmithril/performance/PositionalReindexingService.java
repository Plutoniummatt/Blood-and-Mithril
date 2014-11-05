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
public class PositionalReindexingService {

	/**
	 * Clears down all positional indexes and reindexes all indexed entities
	 */
	public static void reindex() {
		for (World world : Domain.getWorlds().values()) {
			for (PositionalIndexNode node : world.getPositionalIndexMap().getAllNodes()) {
				node.clear();
			}

			for (int individualId : world.getIndividuals()) {
				Individual individual = Domain.getIndividual(individualId);
				individual.updatePositionalIndex();
			}

			for (int itemId : world.getItems()) {
				Item item = Domain.getItem(itemId);
				item.updatePositionalIndex();
			}
			
			for (int propId : world.getProps()) {
				Prop prop = Domain.getProp(propId);
				prop.updatePositionIndex();
			}
		}
	}
}