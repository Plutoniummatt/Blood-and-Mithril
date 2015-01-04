package bloodandmithril.generation.patterns;

import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.TerrainGenerator;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.stone.MagmaTile;
import bloodandmithril.world.topography.tile.tiles.stone.ObsidianTile;

/**
 * Manages layers
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class GlobalLayers {

	/** The Perlin generator used to generate the curves of the layers */
	private static PerlinNoiseGenerator1D noiseGenerator = new PerlinNoiseGenerator1D(1, ParameterPersistenceService.getParameters().getSeed(), 1, 0f);

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

		PerlinNoiseGenerator1D layerGenerator = new PerlinNoiseGenerator1D(layerToUse.a, layerKeyToUse * 100, 1, 0f);

		float noise;
		if (layerKeyToUse - worldTileY < (layerToUse.b - 1) * layerGenerator.generate(worldTileX, 1)) {
			noise = noiseGenerator.generate(layerKeyToUse, 0);
		} else {
			noise = noiseGenerator.generate(layerKeyToUse - layerToUse.b, 0);
		}

		if (noise < 0.5) {
			return new ObsidianTile();
		} else {
			return new MagmaTile();
		}
	}
}
