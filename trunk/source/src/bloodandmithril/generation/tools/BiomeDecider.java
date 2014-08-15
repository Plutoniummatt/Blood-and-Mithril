package bloodandmithril.generation.tools;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.Canyon;
import bloodandmithril.generation.superstructure.Desert;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.generation.superstructure.Underground;
import bloodandmithril.util.Util;
import bloodandmithril.world.World;

/**
 * A tool for deciding which Biome to use.
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class BiomeDecider {

	private static final int cSurfaceStructureHeight = 60;

	/**
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetSurfaceBiome(World world) {

		if(Util.getRandom().nextFloat() > 0.1) {
			return new Canyon(world.getWorldId(), 5, cSurfaceStructureHeight, 20, 50, 300, 3, 30);
		} else {
			return new Desert(world.getWorldId(), 50, cSurfaceStructureHeight, 30, -400, 200);
		}
	}


	/**
	 * Decides which subterranean biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetSubterraneanBiome(World world) {
		return new Underground(world.getWorldId());
	}


	/**
	 * Decides which elevated (in the sky) biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetElevatedBiome(World world) {
		return null;
	}
}
