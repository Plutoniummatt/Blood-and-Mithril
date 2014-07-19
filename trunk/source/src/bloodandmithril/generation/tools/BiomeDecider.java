package bloodandmithril.generation.tools;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.Desert;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.world.World;

/**
 * A tool for deciding which Biome to use.
 *
 * @author Sam
 */
@Copyright("Matthew Peck 2014")
public class BiomeDecider {

	/**
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetBiome(World world) {
		return new Desert(world.getWorldId(), 50, 60, 30, -400, 200);
	}
}
