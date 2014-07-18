package bloodandmithril.generation.superstructure;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.cavesMaxHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.cavesMaxWidth;
import bloodandmithril.core.Copyright;
import bloodandmithril.generation.patterns.Layers;
import bloodandmithril.generation.patterns.UndergroundWithCaves;
import bloodandmithril.generation.tools.RectangularSpaceCalculator;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A region of superstructre where cavity patterns are used
 *
 * @author Sam, Matt
 */
@Copyright("Matthew Peck 2014")
public class Caves extends SuperStructure {
	private static final long serialVersionUID = -9034605400597129907L;

	/**
	 * Constructor
	 */
	public Caves(int worldId) {
		super(worldId);
	}


	@Override
	protected Boundaries findSpace(int startingChunkX, int startingChunkY) {
		// Find space for the cave
		return RectangularSpaceCalculator.calculateBoundaries(
			true,
			startingChunkX,
			startingChunkY,
			cavesMaxWidth,
			cavesMaxHeight,
			Domain.getWorld(worldId).getTopography()
		);
	}


	@Override
	protected void internalGenerate(boolean generatingToRight) {
	}


	@Override
	protected Tile internalGetForegroundTile(int worldTileX, int worldTileY) {
		return UndergroundWithCaves.getTile(worldTileX, worldTileY);
	}


	@Override
	protected Tile internalGetBackgroundTile(int worldTileX, int worldTileY) {
		return Layers.getTile(worldTileX, worldTileY);
	}
}