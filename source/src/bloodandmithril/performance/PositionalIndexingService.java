package bloodandmithril.performance;

import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import com.google.inject.Singleton;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.fluids.FluidParticle;

/**
 * Service to re-index indexable entities.
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2014")
public class PositionalIndexingService {

	/**
	 * Clears down all positional indexes and reindexes all indexed entities
	 */
	public void reindex() {
		for (final World world : Domain.getAllWorlds()) {
			for (final PositionalIndexChunkNode node : world.getPositionalIndexChunkMap().getAllNodes()) {
				node.clear();
			}

			for (final int individualId : world.getIndividuals()) {
				indexInvidivual(Domain.getIndividual(individualId));
			}

			for (final Item item : world.items().getItems()) {
				indexItem(item);
			}

			for (final Prop prop : world.props().getProps()) {
				prop.updatePositionIndex();
			}
			
			for (final FluidParticle fluidParticle : world.fluids().getAllFluidParticles()) {
				indexFluidParticle(fluidParticle);
			}
		}
	}


	public void indexInvidivual(final Individual indi) {
		for (final PositionalIndexChunkNode node : Domain.getWorld(indi.getWorldId()).getPositionalIndexChunkMap().getNearbyNodes(indi.getState().position.x, indi.getState().position.y)) {
			node.removeIndividual(indi.getId().getId());
		}

		Domain.getWorld(indi.getWorldId()).getPositionalIndexChunkMap().get(indi.getState().position.x, indi.getState().position.y).addIndividual(indi.getId().getId());
	}


	public void indexItem(final Item item) {
		for (final PositionalIndexChunkNode node : Domain.getWorld(item.getWorldId()).getPositionalIndexChunkMap().getNearbyNodes(item.getPosition().x, item.getPosition().y)) {
			node.removeItem(item.getId());
		}

		Domain.getWorld(item.getWorldId()).getPositionalIndexChunkMap().get(item.getPosition().x, item.getPosition().y).addItem(item.getId());
	}
	
	
	public void indexFluidParticle(final FluidParticle fluidParticle) {
		for (final PositionalIndexTileNode node : Domain.getWorld(fluidParticle.getWorldId()).getPositionalIndexTileMap().getNearbyNodes(fluidParticle.getPosition().x, fluidParticle.getPosition().y)) {
			node.removeFluidParticle(fluidParticle.getId());
		}
		
		if (fluidParticle.getRadius() < TILE_SIZE) {
			for(int x = convertToWorldTileCoord(fluidParticle.getPosition().x - fluidParticle.getRadius()); x <= convertToWorldTileCoord(fluidParticle.getPosition().x + fluidParticle.getRadius()); x++) {
				for(int y = convertToWorldTileCoord(fluidParticle.getPosition().y - fluidParticle.getRadius()); y <= convertToWorldTileCoord(fluidParticle.getPosition().y + fluidParticle.getRadius()); y++) {
					Domain.getWorld(fluidParticle.getWorldId()).getPositionalIndexTileMap().getWithTileCoords(x, y).addFluidParticle(fluidParticle.getId());
				}
			}
		} else {		
			for(int x = convertToWorldTileCoord(fluidParticle.getPosition().x - fluidParticle.getRadius()); x <= convertToWorldTileCoord(fluidParticle.getPosition().x + fluidParticle.getRadius()); x++) {
				
				float xDifference = fluidParticle.getPosition().x - (x + (
						x * TILE_SIZE > fluidParticle.getPosition().x - fluidParticle.getRadius() &&
						x * TILE_SIZE < fluidParticle.getPosition().x + fluidParticle.getRadius() ? 0 : 1
								)) * TILE_SIZE;
				float topY = fluidParticle.getPosition().y + (float)Math.sqrt(Math.pow(fluidParticle.getRadius(), 2) - Math.pow(xDifference, 2));
				float bottomY = fluidParticle.getPosition().y - (float)Math.sqrt(Math.pow(fluidParticle.getRadius(), 2) - Math.pow(xDifference, 2));
				
				for(int y = convertToWorldTileCoord(bottomY); y <= convertToWorldTileCoord(topY); y++) {
					Domain.getWorld(fluidParticle.getWorldId()).getPositionalIndexTileMap().getWithTileCoords(x, y).addFluidParticle(fluidParticle.getId());
				}
			}
		}
	}
}