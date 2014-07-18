package bloodandmithril.generation.patterns;

import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxLayerHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxLayerStretch;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.maxSurfaceHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.minLayerHeight;
import static bloodandmithril.generation.settings.GlobalGenerationSettings.minLayerStretch;

import java.util.concurrent.ConcurrentSkipListMap;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.tools.PerlinNoiseGenerator1D;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.soil.StandardSoilTile;
import bloodandmithril.world.topography.tile.tiles.stone.GraniteTile;

/**
 * Manages layers
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class Layers {

	/** The Perlin generator used to generate the curves of the layers */
	private static PerlinNoiseGenerator1D noiseGenerator = new PerlinNoiseGenerator1D(1, ParameterPersistenceService.getParameters().getSeed());

	/** Maps the vertical position of the Layer (referencing the top) to the Layer, which is stored as two ints, horizontal stretch and height. */
	public static ConcurrentSkipListMap<Integer, TwoInts> layers;

	/** Makes a new Layer. Layers are stored as two ints, horizontal stretch and height. */
	private static TwoInts getNewLayer() {
		int width = Util.getRandom().nextInt(maxLayerStretch - minLayerStretch) + minLayerStretch;
		int height = Util.getRandom().nextInt(maxLayerHeight - minLayerHeight) + minLayerHeight;
		return new TwoInts(width, height);
	}


	/**
	 * Gets the correct tile type depending on depth/layer
	 */
	public static Tile getTile(int worldTileX, int worldTileY) {

		//If there are no ceiling keys, make a new layer
		if (layers.ceilingEntry(worldTileY) == null) {
			layers.put(maxSurfaceHeight * Topography.CHUNK_SIZE, getNewLayer());
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

		PerlinNoiseGenerator1D layerGenerator = new PerlinNoiseGenerator1D(layerToUse.a, layerKeyToUse * 100);

		float noise;
		if (layerKeyToUse - worldTileY < (layerToUse.b - 1) * layerGenerator.generate(worldTileX, 1)) {
			noise = noiseGenerator.generate(layerKeyToUse, 0);
		} else {
			noise = noiseGenerator.generate(layerKeyToUse - layerToUse.b, 0);
		}

		if (noise < 0.5) {
			return new GraniteTile();
		} else {
			return new StandardSoilTile();
		}
	}
}
