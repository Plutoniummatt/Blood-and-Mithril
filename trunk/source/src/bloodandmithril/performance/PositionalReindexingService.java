package bloodandmithril.performance;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
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

			for (Item item : world.items().getItems()) {
				item.updatePositionalIndex();
			}

			for (Prop prop : world.props().getProps()) {
				prop.updatePositionIndex();
			}

			for (Projectile projectile : world.projectiles().getProjectiles()) {
				projectile.updatePositionIndex();
			}
		}
	}
}