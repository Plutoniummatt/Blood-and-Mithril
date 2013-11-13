package bloodandmithril.generation.tools;

import bloodandmithril.generation.superstructure.Desert;
import bloodandmithril.generation.superstructure.Hills;
import bloodandmithril.generation.superstructure.Plains;
import bloodandmithril.generation.superstructure.SuperStructure;
import bloodandmithril.util.Util;

/**
 * A tool for deciding which Biome to use.
 * 
 * @author Sam
 */
public class BiomeDecider {
	
	
	/**
	 * Constructor
	 */
	public BiomeDecider() {
	}
	
	
	/**
	 * Decides which biome to use.
	 * 
	 * @return - the Biome which was decided.
	 */
	public SuperStructure decideAndGetBiome() {
		float biomeDecider = Util.getRandom().nextFloat();
		
		if (biomeDecider < 0.0f) {
			return new Plains();
		} else if (biomeDecider < 0.0f) {
			return new Hills();
		} else {
			return new Desert();
		}
	}
}
