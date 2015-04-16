package bloodandmithril.generation.biome;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.world.World;

/**
 * A tool for deciding which Biome to use.
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public interface BiomeDecider extends Serializable {

	/**
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetSurfaceBiome(World world);


	/**
	 * Decides which subterranean biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetSubterraneanBiome(World world);


	/**
	 * Decides which elevated (in the sky) biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetElevatedBiome(World world);
}
