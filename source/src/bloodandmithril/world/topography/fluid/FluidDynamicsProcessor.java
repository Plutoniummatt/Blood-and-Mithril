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
		if (!processBottomSpace(fluid)) {
			processSideSpaces(fluid);
		}
	}


	/**
	 * Process the spaces to the left/right of the fluid
	 */
	private void processSideSpaces(Fluid fluid) {
		Tile rightTile = topography.getTile(
			fluid.getTileX() + 1, 
			fluid.getTileY(), 
			true
		);
		
		Tile leftTile = topography.getTile(
			fluid.getTileX() - 1, 
			fluid.getTileY(), 
			true
		);
		
		if (rightTile.isPassable()) {
			Fluid rightFluid = topography.getFluids().get(fluid.getTileX() + 1, fluid.getTileY());
			if (rightFluid == null) {
				if (fluid.getDepth() != 1) {
					fluid.setDepth(fluid.getDepth() / 2);
					Fluid copy = fluid.copy();
					copy.setTileX(fluid.getTileX() + 1);
					copy.setDepth(fluid.getDepth() - fluid.getDepth() / 2);
					topography.getFluids().put(fluid.getTileX() + 1, fluid.getTileY(), copy);
				}
			} else {
				if (rightFluid.getDepth() < fluid.getDepth()) {
					fluid.setDepth(fluid.getDepth() - (fluid.getDepth() - rightFluid.getDepth())/2);
					rightFluid.setDepth(rightFluid.getDepth() + (fluid.getDepth() - rightFluid.getDepth())/2);
				} else if (rightFluid.getDepth() > fluid.getDepth()) {
					fluid.setDepth(fluid.getDepth() + (rightFluid.getDepth() - fluid.getDepth())/2);
					rightFluid.setDepth(rightFluid.getDepth() - (rightFluid.getDepth() - fluid.getDepth())/2);
				}
			}
		}
		
		if (leftTile.isPassable()) {
			Fluid leftFluid = topography.getFluids().get(fluid.getTileX() - 1, fluid.getTileY());
			if (leftFluid == null) {
				if (fluid.getDepth() != 1) {
					fluid.setDepth(fluid.getDepth() / 2);
					Fluid copy = fluid.copy();
					copy.setTileX(fluid.getTileX() - 1);
					copy.setDepth(fluid.getDepth() - fluid.getDepth() / 2);
					topography.getFluids().put(fluid.getTileX() - 1, fluid.getTileY(), copy);
				}
			} else {
				if (leftFluid.getDepth() < fluid.getDepth()) {
					fluid.setDepth(fluid.getDepth() - (fluid.getDepth() - leftFluid.getDepth())/2);
					leftFluid.setDepth(leftFluid.getDepth() + (fluid.getDepth() - leftFluid.getDepth())/2);
				} else if (leftFluid.getDepth() > fluid.getDepth()) {
					fluid.setDepth(fluid.getDepth() + (leftFluid.getDepth() - fluid.getDepth())/2);
					leftFluid.setDepth(leftFluid.getDepth() - (leftFluid.getDepth() - fluid.getDepth())/2);
				}
			}
		}
	}


	/**
	 * Process the space below the fluid, returning true if a fluid could fall completely into the space below
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
				return true;
			} else {
				if (fluidBelow.getDepth() == TILE_SIZE) {
					return false;
				} else if (fluidBelow.getDepth() + fluid.getDepth() > TILE_SIZE) {
					fluidBelow.setDepth(TILE_SIZE);
					fluid.setDepth(fluidBelow.getDepth() + fluid.getDepth() - TILE_SIZE);
					return true;
				} else if (fluidBelow.getDepth() + fluid.getDepth() == TILE_SIZE) {
					fluidBelow.setDepth(TILE_SIZE);
					topography.getFluids().remove(fluid.getTileX(), fluid.getTileY());
					return true;
				}
			}
		}
		return false;
	}
}