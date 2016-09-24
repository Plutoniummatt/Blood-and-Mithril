package bloodandmithril.world;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.fluids.FluidParticle;
import bloodandmithril.world.fluids.FluidStrip;

/**
 * World fluids
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2016")
public class WorldFluids implements Serializable {
	private static final long serialVersionUID = 1244924890817492679L;

	public final int worldId;

	/**
	 * Maps fluid strip ID to the {@link FluidStrip}
	 */
	private final ConcurrentHashMap<Integer, FluidStrip> fluidStrips = new ConcurrentHashMap<>();

	/**
	 * Maps fluid particle ID to a {@link FluidParticle}
	 */
	private final ConcurrentHashMap<Long, FluidParticle> fluidParticles = new ConcurrentHashMap<>();


	/**
	 * Constructor
	 */
	public WorldFluids(final int worldId) {
		this.worldId = worldId;
	}


	/**
	 * @return the {@link FluidStrip} with the given id
	 */
	public final Optional<FluidStrip> getFluidStrip(final int id) {
		if(fluidStrips.get(id) == null) {
			return Optional.absent();
		} else {
			return Optional.of(fluidStrips.get(id));
		}
	}


	/**
	 * @param worldTileX
	 * @param worldTileY
	 *
	 * @return The strip which occupies this tile
	 */
	public final Optional<FluidStrip> getFluidStrip(final int worldTileX, final int worldTileY) {
		for (final Integer stripKey : Domain.getWorld(worldId).getPositionalIndexChunkMap().getWithTileCoords(worldTileX, worldTileY).getAllEntitiesForType(FluidStrip.class)) {
			if (fluidStrips.get(stripKey).occupies(worldTileX, worldTileY)) {
				return Optional.of(fluidStrips.get(stripKey));
			}
		}

		return Optional.absent();
	}


	/**
	 * @return all fluid strips
	 */
	public final Collection<FluidStrip> getAllFluidStrips() {
		return fluidStrips.values();
	}


	/**
	 * @return all fluid particles
	 */
	public final Collection<FluidParticle> getAllFluidParticles() {
		return fluidParticles.values();
	}


	/**
	 * @param strip to add
	 */
	public final void addFluidStrip(final FluidStrip strip) {
		fluidStrips.put(strip.id, strip);
		for (int x = convertToChunkCoord(strip.worldTileX); x <= convertToChunkCoord(strip.worldTileX + strip.width); x++) {
			Domain.getWorld(strip.worldId).getPositionalIndexChunkMap().getWithChunkCoords(x, convertToChunkCoord(strip.worldTileY)).addFluidStrip(strip.id);
		}
	}


	/**
	 * @param key of the strip to remove
	 */
	public final synchronized void removeFluidStrip(final int key) {
		final FluidStrip toRemove = fluidStrips.get(key);
		for (int x = convertToChunkCoord(toRemove.worldTileX); x <= convertToChunkCoord(toRemove.worldTileX + toRemove.width); x++) {
			Domain.getWorld(toRemove.worldId).getPositionalIndexChunkMap().getWithChunkCoords(x, convertToChunkCoord(toRemove.worldTileY)).removeFluidStrip(toRemove.id);
		}
		fluidStrips.remove(key);
	}


	/**
	 * @param toAdd the {@link FluidParticle} to add
	 */
	public final void addFluidParticle(final FluidParticle toAdd) {
		fluidParticles.put(toAdd.id, toAdd);
		indexFluidParticle(toAdd);
	}
	
	
	public final void indexFluidParticle(final FluidParticle toAdd) {
		for(int x = convertToWorldTileCoord(toAdd.position.x - toAdd.getRadius()); x <= convertToWorldTileCoord(toAdd.position.x + toAdd.getRadius()); x++) {
			
			float topY = toAdd.position.y + (float)Math.sqrt(Math.pow(toAdd.getRadius(), 2) - Math.pow(x - toAdd.position.x, 2));
			float bottomY = toAdd.position.y - (float)Math.sqrt(Math.pow(toAdd.getRadius(), 2) - Math.pow(x - toAdd.position.x, 2));
			
			for(int y = convertToWorldTileCoord(bottomY); y <= convertToWorldTileCoord(topY); y++) {
				Domain.getWorld(toAdd.worldId).getPositionalIndexChunkMap().getWithChunkCoords(x, y);
			}
		}
	}


	/**
	 * @param key of the {@link FluidParticle} to remove
	 */
	public final void removeFluidParticle(final long key) {
		fluidParticles.remove(key);
	}


	public Optional<FluidParticle> getFluidParticle(Long id) {
		if(fluidParticles.get(id) == null) {
			return Optional.absent();
		} else {
			return Optional.of(fluidParticles.get(id));
		}
	}
}