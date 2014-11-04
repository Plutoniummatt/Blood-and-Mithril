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

			for (Prop prop : Domain.getProps().values()) {
				Domain.getActiveWorld().getPositionalIndexMap().get(prop.position.x, prop.position.x).removeProp(prop.id);
				Domain.getActiveWorld().getPositionalIndexMap().get(prop.position.x, prop.position.x).addProp(prop.id);
			}

			for (Individual individual : Domain.getIndividuals().values()) {
				individual.updatePositionalIndex();
			}

			for (Item item : Domain.getItems().values()) {
				item.updatePositionalIndex();;
			}
		}
	}
}