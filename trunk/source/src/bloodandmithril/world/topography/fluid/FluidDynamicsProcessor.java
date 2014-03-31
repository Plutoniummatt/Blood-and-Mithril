package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import bloodandmithril.world.topography.Topography;

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
		topography.getFluids().getAllFluids().stream()
		.sorted((e1, e2) -> {
			return e1.y.compareTo(e2.y);
		})
		.forEach(entry -> {
			processSingleFluid(entry.x, entry.y, entry.value);
		});
	}


	/**
	 * Processes a single fluid
	 */
	private void processSingleFluid(int x, int y, Fluid fluid) {
		pressureCascade(x, y, fluid);
		
		if (isFlowable(x, y - 1)) {
			Fluid below = getFluid(x, y - 1);
			if (below == null) {
				topography.getFluids().put(x, y - 1, fluid);
				topography.getFluids().remove(x, y);
			} else {
				if (below.getDepth() == TILE_SIZE) {
					flowSideways(x, y, fluid);
				} else if (TILE_SIZE - below.getDepth() >= fluid.getDepth()) {
					below.add(fluid);
					topography.getFluids().remove(x, y);
				} else {
					below.add(fluid.sub(TILE_SIZE - below.getDepth()));
					if (below.getDepth() == TILE_SIZE) {
						flowSideways(x, y, fluid);
					}
				}
			}
		} else {
			flowSideways(x, y, fluid);
		}
	}


	/**
	 * How do I sideways?
	 */
	private void flowSideways(int x, int y, Fluid fluid) {
		float leftPressureGradient = calculatePressureGradient(x - 1, y, fluid);
		float rightPressureGradient = calculatePressureGradient(x + 1, y, fluid);
		
		
	}


	private float calculatePressureGradient(int x, int y, Fluid fluid) {
		if (isFlowable(x, y)) {
			Fluid adjacent = getFluid(x, y);
			if (adjacent == null) {
				return fluid.getPressure();
			} else {
				return fluid.getPressure() - adjacent.getPressure();
			}
		} else {
			return 0f;
		}
	}


	/**
	 * Calculates pressure of a stack of {@link Fluid}s
	 */
	private void pressureCascade(int x, int y, Fluid fluid) {
		if (getFluid(x, y + 1) == null) {
			fluid.setPressure(fluid.getDepth());
			Fluid bottom = getFluid(x, y - 1);
			if (bottom != null) {
				bottom.setPressure(fluid.getPressure() + bottom.getDepth());
				pressureCascade(x, y - 1, bottom);
			}
		} else {
			Fluid bottom = getFluid(x, y - 1);
			if (bottom != null) {
				bottom.setPressure(fluid.getPressure() + bottom.getDepth());
				pressureCascade(x, y - 1, bottom);
			}
		}
	}


	private boolean isFlowable(int x, int y) {
		try {
			return topography.getTile(x, y, true).isPassable();
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	
	private Fluid getFluid(int x, int y) {
		return topography.getFluids().get(x, y);
	}
}