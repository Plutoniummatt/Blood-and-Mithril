package bloodandmithril.world.topography;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static bloodandmithril.world.topography.Topography.convertToChunkTileCoord;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
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
		}
	}
}