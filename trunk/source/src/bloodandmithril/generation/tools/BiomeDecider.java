package bloodandmithril.generation.tools;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.superstructure.Desert;
import bloodandmithril.generation.superstructure.Hills;
import bloodandmithril.generation.superstructure.Plains;
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
	 * Decides which biome to use.
	 *
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetBiome(World world) {
		float biomeDecider = Util.getRandom().nextFloat();

		if (biomeDecider < 0.0f) {
			return new Plains(world.getWorldId());
		} else if (biomeDecider < 0.0f) {
			return new Hills(world.getWorldId());
		} else {
			return new Desert(world.getWorldId());
		}
	}
}
