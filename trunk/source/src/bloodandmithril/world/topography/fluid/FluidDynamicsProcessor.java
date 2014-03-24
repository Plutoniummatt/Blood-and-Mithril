package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Class responsible for processing fluid dynamics.
 *
 * @author Matt
 */
public class FluidDynamicsProcessor {

	/** The {@link Topography} instance this processor is responsible for */
	private Topography topography;

	/**
	 * Constructor
	 */
	public FluidDynamicsProcessor(Topography topography) {
		this.topography = topography;
	}
	
	
	/**
	 * Process all {@link Fluid}s on the {@link #topography}
	 */
	public void process() {
		for (DualKeyEntry<Integer, Integer, Fluid> fluid : topography.getFluids().getAllEntries()) {
			processSingleFluid(fluid.value);
		}
	}
	
	
	/**
	 * Processes a single {@link Fluid}
	 */
	private void processSingleFluid(Fluid fluid) {
		if (processBottomSpace(fluid)) {
			processSideSpace(fluid.getTileX() + 1, fluid);
			processSideSpace(fluid.getTileX() - 1, fluid);
		}
	}


	/**
	 * Process the spaces to the left/right of the fluid
	 */
	private void processSideSpace(int x, Fluid fluid) {
		Tile sideTile = topography.getTile(
			x, 
			fluid.getTileY(), 
			true
		);
		
		if (sideTile != null && sideTile.isPassable()) {
			Fluid sideFluid = topography.getFluids().get(x, fluid.getTileY());
			if (sideFluid == null) {
				Fluid copy = fluid.copy();
				copy.setDepth(fluid.getDepth() / 2);
				copy.setTileX(x);
				topography.getFluids().put(x, fluid.getTileY(), copy);
				
				fluid.setDepth(fluid.getDepth() - fluid.getDepth() / 2);
			} else {
				int sideDepth = sideFluid.getDepth();
				int fluidDepth = fluid.getDepth();
				
				if (sideDepth + 1 < fluidDepth) {
					sideFluid.setDepth((fluidDepth - sideDepth) / 2 + sideDepth);
					fluid.setDepth(fluidDepth - (fluidDepth - sideDepth) / 2);
				} else if (sideDepth + 1 == fluidDepth) {
					sideFluid.setDepth(sideDepth + 1);
					fluid.setDepth(fluidDepth - 1);
				} else if (sideDepth > fluidDepth) {
					sideFluid.setDepth(sideDepth - (fluidDepth - sideDepth) / 2);
					fluid.setDepth((fluidDepth - sideDepth) / 2 + fluidDepth);
				}
			}
		}
	}


	/**
	 * Process the space below the fluid, returning true if the fluid is to spread sideways into adjacent tiles
	 */
	private boolean processBottomSpace(Fluid fluid) {
		Tile tileBelow = topography.getTile(
			fluid.getTileX(), 
			fluid.getTileY() - 1, 
			true
		);
		
		if (tileBelow.isPassable()) {
			Fluid fluidBelow = topography.getFluids().get(fluid.getTileX(), fluid.getTileY() - 1);
			
			if (fluidBelow == null) {
				topography.getFluids().remove(fluid.getTileX(), fluid.getTileY());
				topography.getFluids().put(fluid.getTileX(), fluid.getTileY() - 1, fluid);
				fluid.setTileY(fluid.getTileY() - 1);
				return false;
			} else {
				int belowDepth = fluidBelow.getDepth();
				int fluidDepth = fluid.getDepth();
				
				if (fluidBelow.getDepth() >= TILE_SIZE - 1) {
					return true;
				} else if (belowDepth + fluidDepth > TILE_SIZE) {
					fluid.setDepth(belowDepth + fluidDepth - TILE_SIZE);
					fluidBelow.setDepth(TILE_SIZE);
					return false;
				} else if (belowDepth + fluidDepth == TILE_SIZE) {
					fluidBelow.setDepth(TILE_SIZE);
					fluid.setDepth(0);
					topography.getFluids().remove(fluid.getTileX(), fluid.getTileY());
					return false;
				} else if (belowDepth + fluidDepth < TILE_SIZE) {
					fluidBelow.setDepth(belowDepth + fluidDepth);
					topography.getFluids().remove(fluid.getTileX(), fluid.getTileY());
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	public static void main(String[] args) {
		for (char j = 0; j <= Byte.MAX_VALUE; j++) {
			System.out.println(Character.toString(j));
		}
	}
}