package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.world.topography.Topography;

/**
 * Class responsible for processing fluid dynamics.
 *
 * @author Matt
 */
public class FluidDynamicsProcessor {

	/** The {@link Topography} instance this processor is responsible for */
	private Topography topography;
	
	/** Whether to reverse sort in the x-direction once y-direction has been sorted */
	private boolean xComparatorReverse;

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
		xComparatorReverse = !xComparatorReverse;
		topography.getFluids().getAllFluids().stream()
		.sorted((e1, e2) -> {
			return e1.y.compareTo(e2.y) == 0 ? (xComparatorReverse ? e2.x.compareTo(e1.x) : e1.x.compareTo(e2.x)) : e1.y.compareTo(e2.y);
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
					pressureFlow(x, y, fluid);
				} else if (TILE_SIZE - below.getDepth() >= fluid.getDepth()) {
					below.add(fluid);
					topography.getFluids().remove(x, y);
				} else {
					below.add(fluid.sub(TILE_SIZE - below.getDepth()));
					if (below.getDepth() == TILE_SIZE) {
						pressureFlow(x, y, fluid);
					}
				}
			}
		} else {
			pressureFlow(x, y, fluid);
		}
	}


	/**
	 * How do I sideways?
	 */
	private void pressureFlow(int x, int y, Fluid fluid) {
		Vector2 netForce = calculateNetForceOn(x, y, fluid);
	}


	private Vector2 calculateNetForceOn(int x, int y, Fluid fluid) {
		return new Vector2(
			calculateForceFrom(x - 1, y) - calculateForceFrom(x + 1, y),
			calculateForceFrom(x, y - 1) - calculateForceFrom(x, y + 1) - fluid.getDepth() // Last term for gravity
		);
	}
	
	
	private float calculateForceFrom(int x, int y) {
		if (isFlowable(x, y)) {
			Fluid adjacent = getFluid(x, y);
			if (adjacent == null) {
				return 0f;
			} else {
				return adjacent.getPressure();
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
			fluid.setPressure(0f);
			Fluid bottom = getFluid(x, y - 1);
			if (bottom != null) {
				bottom.setPressure(fluid.getDepth() + fluid.getPressure());
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