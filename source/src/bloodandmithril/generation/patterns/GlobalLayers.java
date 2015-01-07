package bloodandmithril.generation.patterns;

import static bloodandmithril.world.topography.Topography.CHUNK_SIZE;

import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.TerrainGenerator;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;
import bloodandmithril.world.topography.tile.tiles.stone.LimeStoneTile;
import bloodandmithril.world.topography.tile.tiles.stone.MagmaTile;
import bloodandmithril.world.topography.tile.tiles.stone.ObsidianTile;

/**
 * Manages layers
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class GlobalLayers {

	/** Maps the vertical position of the Layer (referencing the top) to the Layer, which is stored as two ints, horizontal stretch and height. */
	public static ConcurrentSkipListMap<Integer, TwoInts> layers;

	/** Makes a new Layer. Layers are stored as two ints, horizontal stretch and height. */
	private static TwoInts getNewLayer() {
		int width = Util.getRandom().nextInt(30) + 10;
		int height = Util.getRandom().nextInt(30) + 10;
		return new TwoInts(width, height);
	}


	/**
	 * Gets the correct tile type depending on depth/layer
	 */
	public static Tile getTile(int worldTileX, int worldTileY) {

		//If there are no ceiling keys, make a new layer
		if (layers.ceilingEntry(worldTileY) == null) {
			layers.put(TerrainGenerator.maxSurfaceHeightInChunks * Topography.CHUNK_SIZE, getNewLayer());
		}

		while (layers.ceilingKey(worldTileY) > worldTileY + layers.ceilingEntry(worldTileY).getValue().b) {
			layers.put(layers.ceilingKey(worldTileY) - layers.ceilingEntry(worldTileY).getValue().b, getNewLayer());
		}

		int layerKeyToUse = layers.ceilingKey(worldTileY);
		TwoInts layerToUse = layers.get(layerKeyToUse);

		return determineTile(worldTileX, worldTileY, layerKeyToUse, layerToUse);
	}


	/**
	 * Determines which tile should be returned.
	 */
	private static Tile determineTile(int worldTileX, int worldTileY, int layerKeyToUse, TwoInts layerToUse) {

		PerlinNoiseGenerator1D layerGenerator = new PerlinNoiseGenerator1D(
			layerToUse.a,
			layerKeyToUse * 100,
			1,
			0f
		);

		int layerHeight;
		int firstLayerHeight = 0;
		int secondLayerHeight = -10 * CHUNK_SIZE;
		int thirdLayerHeight = -25 * CHUNK_SIZE;
		int lastLayerHeight = -50 * CHUNK_SIZE;

		if (layerKeyToUse - worldTileY < (layerToUse.b - 1)	* layerGenerator.generate(worldTileX, 1)) {
			layerHeight = layerKeyToUse;
		} else {
			layerHeight = layerKeyToUse - layerToUse.b;
		}

		if (layerHeight > firstLayerHeight) {
			return new StandardSoilTile();
		} else if (layerHeight <= firstLayerHeight && layerHeight > secondLayerHeight) {
			return new GraniteTile();
		} else if (layerHeight <= secondLayerHeight && layerHeight > thirdLayerHeight) {
			return new LimeStoneTile();
		} else if (layerHeight <= thirdLayerHeight && layerHeight > lastLayerHeight) {
			return new ObsidianTile();
		} else {
			return new MagmaTile();
		}
	}
}
