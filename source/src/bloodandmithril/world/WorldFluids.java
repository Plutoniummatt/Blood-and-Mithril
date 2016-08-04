package bloodandmithril.world;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;

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

	private final ConcurrentHashMap<Integer, FluidStrip> fluidStrips = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Long, FluidParticle> fluidParticles = new ConcurrentHashMap<>();
	
	/**
	 * Constructor
	 */
	public WorldFluids(int worldId) {
		this.worldId = worldId;
	}
	
	
	/**
	 * @return the {@link FluidStrip} with the given id
	 */
	public final FluidStrip getFluidStrip(int id) {
		return fluidStrips.get(id);
	}
	
	
	/**
	 * @param worldTileX
	 * @param worldTileY
	 * 
	 * @return The strip which occupies this tile
	 */
	public final Optional<FluidStrip> getFluid(int worldTileX, int worldTileY) {
		for (Integer stripKey : Domain.getWorld(worldId).getPositionalIndexMap().getWithTileCoords(worldTileX, worldTileY).getAllEntitiesForType(FluidStrip.class)) {
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
	public final void addFluidStrip(FluidStrip strip) {
		fluidStrips.put(strip.id, strip);
		for (int x = convertToChunkCoord(strip.worldTileX); x <= convertToChunkCoord(strip.worldTileX + strip.width); x++) {
			Domain.getWorld(strip.worldId).getPositionalIndexMap().getWithChunkCoords(x, convertToChunkCoord(strip.worldTileY)).addFluidStrip(strip.id);
		}
	}
	
	
	/**
	 * @param key of the strip to remove
	 */
	public final void removeFluidStrip(int key) {
		FluidStrip removed = fluidStrips.remove(key);
		for (int x = convertToChunkCoord(removed.worldTileX); x <= convertToChunkCoord(removed.worldTileX + removed.width); x++) {
			Domain.getWorld(removed.worldId).getPositionalIndexMap().getWithChunkCoords(x, convertToChunkCoord(removed.worldTileY)).addFluidStrip(removed.id);
		}
	}

	
	/**
	 * @param toAdd the {@link FluidParticle} to add
	 */
	public final void addFluidParticle(FluidParticle toAdd) {
		fluidParticles.put(toAdd.id, toAdd);
	}
	
	
	/**
	 * @param key of the {@link FluidParticle} to remove
	 */
	public final void removeFluidParticle(long key) {
		fluidParticles.remove(key);
	}
}