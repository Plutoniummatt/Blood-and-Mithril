package bloodandmithril.generation.superstructure;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2016")
public class Sky extends SuperStructure {
	private static final long serialVersionUID = 3773635748047662939L;

	private int cHeight = 50;
	private int cWidth = 50;

	/**
	 * @param worldId - the ID of the world.
	 */
	public Sky(final int worldId) {
		super(worldId);
	}


	@Override
	protected Boundaries findSpace(final int startingChunkX, final int startingChunkY) {
		return RectangularSpaceCalculator.calculateBoundariesConfineWithinTwoHeights(
			true,
			startingChunkX,
			startingChunkY,
			cWidth,
			cHeight,
			Integer.MAX_VALUE,
			ChunkGenerator.maxSurfaceHeightInChunks,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(final boolean generatingToRight) {
	}


	@Override
	protected Tile internalGetForegroundTile(final int worldTileX, final int worldTileY) {
		return new Tile.EmptyTile();
	}


	@Override
	protected Tile internalGetBackgroundTile(final int worldTileX, final int worldTileY) {
		return new Tile.EmptyTile();
	}
}
