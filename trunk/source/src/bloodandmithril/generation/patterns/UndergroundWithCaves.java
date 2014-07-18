package bloodandmithril.generation.patterns;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.tools.PerlinNoiseGenerator2D;
import bloodandmithril.world.topography.tile.Tile;

@Copyright("Matthew Peck 2014")
public class UndergroundWithCaves {

	private static PerlinNoiseGenerator2D spaceArea = new PerlinNoiseGenerator2D(100, 0.6f, 3);
	private static PerlinNoiseGenerator2D mess = new PerlinNoiseGenerator2D(10, 0.5f, 4);

	private static float messDensity = 0.55f;
	private static float caveEntranceFrequency = 5f;

	/**
	 * Gets the tile type calculated.
	 */
	public static Tile getTile(int worldTileX, int worldTileY) {

		// The amount of debris/mess there should be calculated depending on the height of tile.
		float someNumber = worldTileY > -(messDensity*100) ? (worldTileY - (caveEntranceFrequency - 1) * messDensity * 100f) / -(caveEntranceFrequency * 100f) : messDensity;

		if (spaceArea.generate(worldTileX, worldTileY) < 0.52f &&
		    spaceArea.generate(worldTileX, worldTileY) > 0.48f &&
		    mess.generate(worldTileX, worldTileY) < someNumber) {
			return new Tile.EmptyTile();
		} else {
			return Layers.getTile(worldTileX, worldTileY);
		}
	}
}