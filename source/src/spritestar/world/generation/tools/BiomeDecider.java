package spritestar.world.generation.tools;

import spritestar.util.Util;
import spritestar.world.generation.SuperStructure;
import spritestar.world.generation.superstructures.Desert;
import spritestar.world.generation.superstructures.Hills;
import spritestar.world.generation.superstructures.Plains;

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
		
		if (biomeDecider < 0.01f) {
			return new Plains();
		} else if (biomeDecider < 0.02f) {
			return new Hills();
		} else {
			return new Desert();
		}
	}
}
