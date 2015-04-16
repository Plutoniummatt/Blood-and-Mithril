package bloodandmithril.generation.biome;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.Desert;
import bloodandmithril.generation.superstructure.Sky;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.generation.superstructure.Underground;
import bloodandmithril.world.World;

/**
 * Default implementation of {@link BiomeDecider}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class DefaultBiomeDecider implements BiomeDecider {
	
	private static final int cSurfaceStructureHeight = 60;

	/**
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	@Override
	public SuperStructure decideAndGetSurfaceBiome(World world) {
		return new Desert(world.getWorldId(), 50, cSurfaceStructureHeight, 30);
	}


	/**
	 * Decides which subterranean biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	@Override
	public SuperStructure decideAndGetSubterraneanBiome(World world) {
		return new Underground(world.getWorldId());
	}


	/**
	 * Decides which elevated (in the sky) biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	@Override
	public SuperStructure decideAndGetElevatedBiome(World world) {
		return new Sky(world.getWorldId());
	}
}