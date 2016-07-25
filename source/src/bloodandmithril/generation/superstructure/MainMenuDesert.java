package bloodandmithril.generation.superstructure;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.Topography;

/**
 * Desert used for the main menu screen
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class MainMenuDesert extends TestSuperStructure {
	private static final long serialVersionUID = 1482427063691368237L;

	/**
	 * Constructor
	 */
	public MainMenuDesert(int worldId, int cWidth, int cHeight, int tDuneVariationHeight) {
		super(worldId, cWidth, cHeight, tDuneVariationHeight);
	}
	
	
	@Override
	protected void internalGenerate(boolean generatingToRight) {
		int rightMostTile = (getBoundaries().right + 1) * Topography.CHUNK_SIZE - 1;
		int leftMostTile = getBoundaries().left * Topography.CHUNK_SIZE;

		generateSurface(generatingToRight, rightMostTile, leftMostTile);
	}
}