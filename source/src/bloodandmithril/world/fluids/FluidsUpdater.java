package bloodandmithril.world.fluids;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.WorldFluids;
import bloodandmithril.world.topography.Topography;

/**
 * Service class for updating fluid columns
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FluidsUpdater {

	/**
	 * Updates all fluids on the world
	 *
	 * @param worldFluids
	 */
	public void update(final WorldFluids worldFluids) {
		for (final FluidColumn fluidColumn : worldFluids.getAllFluids()) {
			normalizeHeight(fluidColumn);
		}
	}


	/**
	 * Normalizes the height of the column, meaning the height will be the minimum allowed height given the fluid's uncompressed volume
	 */
	private void normalizeHeight(final FluidColumn fluidColumn) {

		// Volume defined in pixels -> 1 tile worth of volume = TILE_SIZE * TILE_SIZE
		final int tileVolume = Topography.TILE_SIZE * Topography.TILE_SIZE;
		int minHeight = fluidColumn.getVolume() / tileVolume;
		minHeight = minHeight + fluidColumn.getVolume() % tileVolume == 0 ? 0 : 1;

		if (fluidColumn.getHeight() > minHeight) {
			fluidColumn.setHeight(minHeight);
		}
	}
}