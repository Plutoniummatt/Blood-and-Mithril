package bloodandmithril.generation.superstructure;

import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

public class Sky extends SuperStructure {
	private static final long serialVersionUID = 3773635748047662939L;
	
	private int cHeight = 50;
	private int cWidth = 50;

	/**
	 * @param worldId - the ID of the world.
	 */
	public Sky(int worldId) {
		super(worldId);
	}

	
	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
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
	protected void internalGenerate(boolean generatingToRight) {
	}

	
	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		return new Tile.EmptyTile();
	}

	
	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		return new Tile.EmptyTile();
	}
}
