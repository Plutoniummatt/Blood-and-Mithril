package bloodandmithril.world.topography;

import static bloodandmithril.world.topography.Topography.convertToChunkCoord;
import static bloodandmithril.world.topography.Topography.convertToChunkTileCoord;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.PropPlacementService;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

/**
 * Handles {@link Tile} deletions on {@link Topography}
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class DeleteTileService {

	@Inject private ChangeTileService changeTileService;

	/**
	 * Deletes a tile at this location
	 *
	 * @param worldX
	 * @param worldY
	 * @return The tile which was deleted.
	 */
	public final Tile deleteTile(final int worldId, final float worldX, final float worldY, final boolean foreGround, final boolean forceRemove) {
		final Topography topography = Domain.getWorld(worldId).getTopography();

		synchronized (topography) {
			final int chunkX = convertToChunkCoord(worldX);
			final int chunkY = convertToChunkCoord(worldY);
			final int tileX = convertToChunkTileCoord(worldX);
			final int tileY = convertToChunkTileCoord(worldY);

			if (!forceRemove && preventedByProps(worldId, worldX, worldY)) {
				return null;
			}

			try {
				if (topography.getTile(worldX, worldY, foreGround) instanceof EmptyTile) {
					return null;
				}
				final Tile tile = topography.getChunkMap().get(chunkX).get(chunkY).getTile(tileX, tileY, foreGround);
				topography.getChunkMap().get(chunkX).get(chunkY).deleteTile(tileX, tileY, foreGround);
				Logger.generalDebug("Deleting tile at (" + convertToWorldTileCoord(chunkX, tileX) + ", " + convertToWorldTileCoord(chunkY, tileY) + "), World coord: (" + worldX + ", " + worldY + ")", LogLevel.TRACE);
				if (!forceRemove) {
					removeProps(worldId, worldX, worldY);
				}
				return tile;

			} catch (final NoTileFoundException e) {
				Logger.generalDebug("can't delete a null tile", LogLevel.WARN);
				return null;
			}
		}
	}


	private final boolean preventedByProps(final int worldId, final float worldX, final float worldY) {
		final Tile deletedTile = deleteTile(worldId, worldX, worldY, true, true);

		final MutableBoolean b = new MutableBoolean();
		b.setValue(false);

		final World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, worldX, worldY).forEach(id -> {
			final Prop prop = world.props().getProp(id);

			boolean canPlace = false;
			try {
				canPlace = Wiring.injector().getInstance(PropPlacementService.class).canPlaceAtCurrentPosition(prop);
			} catch (final NoTileFoundException e) {}

			if (!canPlace && prop.preventsMining) {
				b.setValue(true);
			}
		});

		if (deletedTile != null) {
			changeTileService.changeTile(worldId, worldX, worldY, true, deletedTile);
		}

		return b.booleanValue();
	}


	private final void removeProps(final int worldId, final float worldX, final float worldY) {
		final Tile deletedTile = deleteTile(worldId, worldX, worldY, true, true);

		final World world = Domain.getWorld(worldId);
		world.getPositionalIndexMap().getNearbyEntityIds(Prop.class, worldX, worldY).forEach(id -> {
			final Prop prop = world.props().getProp(id);

			boolean canPlace = false;
			try {
				canPlace = Wiring.injector().getInstance(PropPlacementService.class).canPlaceAtCurrentPosition(prop);
			} catch (final NoTileFoundException e) {}

			if (!canPlace && !prop.preventsMining) {
				world.props().removeProp(prop.id);
			}
		});

		if (deletedTile != null) {
			changeTileService.changeTile(worldId, worldX, worldY, true, deletedTile);
		}
	}
}