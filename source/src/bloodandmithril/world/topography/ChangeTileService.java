package bloodandmithril.world.topography;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static bloodandmithril.world.topography.Topography.convertToChunkTileCoord;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.fluids.FluidStrip;
import bloodandmithril.world.fluids.FluidStripPopulator;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Changes {@link Tile}s on {@link Topography}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ChangeTileService {

	@Inject private FluidStripPopulator fluidStripPopulator;

	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public final void changeTile(final int worldId, final float worldX, final float worldY, final boolean foreGround, final Class<? extends Tile> toChangeTo) {
		try {
			changeTile(worldId, worldX, worldY, foreGround, toChangeTo.newInstance());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Changes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 */
	public final void changeTile(final int worldId, final float worldX, final float worldY, final boolean foreGround, final Tile toChangeTo) {
		final Topography topography = Domain.getWorld(worldId).getTopography();

		synchronized (topography) {
			if (toChangeTo instanceof EmptyTile) {
				return;
			}

			final int chunkX = convertToChunkCoord(worldX);
			final int chunkY = convertToChunkCoord(worldY);
			final int tileX = convertToChunkTileCoord(worldX);
			final int tileY = convertToChunkTileCoord(worldY);

			try {
				topography.getChunkMap().get(chunkX).get(chunkY).changeTile(tileX, tileY, foreGround, toChangeTo);
			} catch (final NullPointerException e) {
				Logger.generalDebug("can't change a null tile", LogLevel.WARN);
			}
			if(foreGround) {
				final Optional<FluidStrip> stripOn = Domain.getWorld(worldId).fluids().getFluidStrip(convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY));
				if(stripOn.isPresent()) {
					if(!fluidStripPopulator.splitFluidStrip(Domain.getWorld(worldId), stripOn.get(), convertToWorldTileCoord(worldX))) {
						final Optional<FluidStrip> stripAbove = Domain.getWorld(worldId).fluids().getFluidStrip(convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY) + 1);
						if(stripAbove.isPresent()) {
							stripAbove.get().addVolume(stripOn.get().getVolume());
						} else {
							fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY) + 1, stripOn.get().getVolume());
						}
						Domain.getWorld(worldId).fluids().removeFluidStrip(stripOn.get().id);
					}
				} else {
					fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX) - 1, convertToWorldTileCoord(worldY), 0f);
					fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX) + 1, convertToWorldTileCoord(worldY), 0f);
					fluidStripPopulator.createFluidStrip(Domain.getWorld(worldId), convertToWorldTileCoord(worldX), convertToWorldTileCoord(worldY) + 1, 0f);
				}
			}
		}
	}
}