package bloodandmithril.world.fluids;

import com.google.common.base.Optional;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;
import bloodandmithril.world.WorldFluids;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * @author Sam
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FluidStripPopulator {
	
	
	/**
	 * Given tileX and tileY, determines if position is valid for a {@link FluidStrip}, instantiating one if valid, and populates the {@link WorldFluids}
	 * with a full width {@link FluidStrip} that covers the bottom most empty "layer" that is valid for a {@link FluidStrip}
	 */
	public Optional<FluidStrip> createFluidStripIfBase(World world, int tileX, int tileY, float volumePercent) throws NoTileFoundException {
		int left = tileX;
		int right = tileX;
		
		if (!world.fluids().getFluidStrip(tileX, tileY).isPresent() && world.getTopography().getTile(tileX, tileY, true).isPassable()) {
			try {
				// left
				while (world.getTopography().getTile(left, tileY, true).isPassable() && !world.getTopography().getTile(left, tileY - 1, true).isPassable()) {
					left--;
				}

				// right
				while (world.getTopography().getTile(right, tileY, true).isPassable() && !world.getTopography().getTile(right, tileY - 1, true).isPassable()) {
					right++;
				}

				if (left == right) {
					return Optional.absent();
				}

				if (!world.getTopography().getTile(right, tileY, true).isPassable() && !world.getTopography().getTile(left, tileY, true).isPassable()) {
					FluidStrip strip = new FluidStrip(left + 1, tileY, right - left - 1, (right - left - 1) * volumePercent, world.getWorldId());
					world.fluids().addFluidStrip(strip);
					return Optional.of(strip);
				}
			} catch (NoTileFoundException e) {
				return Optional.absent();
			}
		}
		
		return Optional.absent();
	}

	
	/**
	 * Given tileX and tileY, determines if position is valid for a {@link FluidStrip}, instantiating one if valid, and populates the {@link WorldFluids}
	 * with a full width {@link FluidStrip} that covers the bottom most empty "layer" that is valid for a {@link FluidStrip}
	 */
	public Optional<FluidStrip> createFluidStrip(World world, int tileX, int tileY, float volume) {
		int left = tileX;
		int right = tileX;
		
		try {
			if (!world.fluids().getFluidStrip(tileX, tileY).isPresent()) {
				if(world.getTopography().getTile(tileX, tileY, true).isPassable()) {
					// left
					while (
						(world.getTopography().getTile(left, tileY, true).isPassable() && !world.fluids().getFluidStrip(left, tileY).isPresent()) && 
						(!world.getTopography().getTile(left, tileY - 1, true).isPassable() || world.fluids().getFluidStrip(left, tileY - 1).isPresent())
					) {
						left--;
					}
					
					// right
					while (
						(world.getTopography().getTile(right, tileY, true).isPassable() && !world.fluids().getFluidStrip(right, tileY).isPresent()) && 
						(!world.getTopography().getTile(right, tileY - 1, true).isPassable() || world.fluids().getFluidStrip(right, tileY - 1).isPresent())
					) {
						right++;
					}
					
					if (left == right) {
						return Optional.absent();
					}
					
					if ((!world.getTopography().getTile(right, tileY, true).isPassable() || world.fluids().getFluidStrip(right, tileY).isPresent()) && 
						(!world.getTopography().getTile(left, tileY, true).isPassable() || world.fluids().getFluidStrip(left, tileY).isPresent())) 
					{
						FluidStrip strip = new FluidStrip(left + 1, tileY, right - left - 1, volume, world.getWorldId());
						world.fluids().addFluidStrip(strip);
						return Optional.of(strip);
					}
					
					Optional<FluidStrip> stripBelow = world.fluids().getFluidStrip(tileX, tileY - 1);
	
					if (stripBelow.isPresent()) {
						int restrictedLeft = Math.max(left + 1, stripBelow.get().worldTileX);
						int restrictedRight = Math.min(right, stripBelow.get().worldTileX + stripBelow.get().width);
						FluidStrip strip = new FluidStrip(restrictedLeft, tileY, restrictedRight - restrictedLeft, volume, world.getWorldId());
						world.fluids().addFluidStrip(strip);
						return Optional.of(strip);
					}
				}
			}
		} catch (NoTileFoundException e) {
			return Optional.absent();
		}

		return Optional.absent();
	}
}
