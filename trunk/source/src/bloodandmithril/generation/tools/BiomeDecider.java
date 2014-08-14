package bloodandmithril.generation.tools;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.Canyon;
import bloodandmithril.generation.superstructure.Desert;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.util.Util;
import bloodandmithril.world.World;

/**
 * A tool for deciding which Biome to use.
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class BiomeDecider {

	/**
	 * TODO - TerrainGenerator always generates surface structures from one of the top corners of the superstructure, this means that
	 * the height of ALL surface structures need to be the same, this restriction has negative consequences, set the Canyon height to 20 and desert to 60,
	 * and see what happens as a canyon is generated after the desert.
	 */
	private static final int cSurfaceStructureHeight = 60;

	/**
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetBiome(World world) {

		if(Util.getRandom().nextFloat() > 0.5) {
			return new Canyon(world.getWorldId(), 5, cSurfaceStructureHeight, 20, 50, 300, 3, 30);
		} else {
			return new Desert(world.getWorldId(), 50, cSurfaceStructureHeight, 30, -400, 200);
		}
	}
}
