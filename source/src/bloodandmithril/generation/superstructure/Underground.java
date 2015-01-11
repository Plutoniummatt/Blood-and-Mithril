package bloodandmithril.generation.superstructure;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.components.Cavern;
import bloodandmithril.generation.patterns.GlobalLayers;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.stone.CrystalTile;

/**
 * A region of superstructre where cavity patterns are used
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public class Underground extends SuperStructure {
	private static final long serialVersionUID = -9034605400597129907L;

	/**
	 * Constructor
	 */
	public Underground(int worldId) {
		super(worldId);
	}


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		// Find space for the cave
		return RectangularSpaceCalculator.calculateBoundariesForUnderground(
			true,
			startingChunkX,
			startingChunkY,
			20,
			20,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
		int centerX = (getBoundaries().left + (getBoundaries().right - getBoundaries().left) / 2) * Topography.CHUNK_SIZE;
		int centerY = (getBoundaries().bottom + (getBoundaries().top - getBoundaries().bottom) / 2) * Topography.CHUNK_SIZE;
		getComponents().add(new Cavern(
			new Boundaries(centerY + 150, centerY - 150, centerX - 200, centerX + 200),
			getStructureKey(),
			15,
			new Boundaries(centerY + 100, centerY - 100, centerX - 150, centerX + 150),
			30,
			10,
			40,
			CrystalTile.class
				
		));
		
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		return GlobalLayers.getTile(worldTileX, worldTileY);
	}


	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		return GlobalLayers.getTile(worldTileX, worldTileY);
	}
}