package bloodandmithril.generation.biome;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.MainMenuDesert;
import bloodandmithril.generation.superstructure.Sky;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.generation.superstructure.Underground;
import bloodandmithril.world.World;

/**
 * Main menu implementation of {@link BiomeDecider}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class MainMenuBiomeDecider implements BiomeDecider {
	
	private static final int cSurfaceStructureHeight = 60;

	/**
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	@Override
	public SuperStructure decideAndGetSurfaceBiome(World world) {
		return new MainMenuDesert(world.getWorldId(), 50, cSurfaceStructureHeight, 10);
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