package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.min;
import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;
import bloodandmithril.world.topography.Topography;

/**
 * Class responsible for processing fluid dynamics.
 *
 * @author Matt
 */
public class FluidDynamicsProcessor {

	/** The {@link Topography} instance this processor is responsible for */
	private Topography topography;
	
	/** The frozen snapshot of the current state of the {@link FluidMap} */
	private FluidMap currentSnapshot;
	
	/** Constants used */
	private static final float MAX_DEPTH = TILE_SIZE;
	private static final float MAX_COMPRESSION = TILE_SIZE * 0.02f;
	private static final float MAX_FLOW = TILE_SIZE;
	private static final float MIN_FLOW = 0.5f;
	
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
		currentSnapshot = topography.getFluids().deepCopy();
		
		System.out.println(currentSnapshot.getAllFluids().stream().mapToDouble(entry -> {
			return entry.value.getDepth();
		}).sum());
		
		currentSnapshot.getAllFluids().stream()
		.sorted((e1, e2) -> {
			return e1.y.compareTo(e2.y);
		})
		.forEach(entry -> {
			processFluid(entry);
		});
	}


	/**
	 * Process a single fluid
	 */
	private void processFluid(DualKeyEntry<Integer, Integer, Fluid> entry) {
		flow(entry.x, entry.y, entry.value);
		
		if (entry.value.getDepth() < 0.1f) {
			topography.getFluids().remove(entry.x, entry.y);
		}
	}


	/**
	 * How do I flow?
	 */
	private void flow(int x, int y, Fluid fluid) {
		float flow = 0f;
		float currentDepth = getSnapshotDepth(x, y);

		// The block below this one
		if (isFlowable(x, y - 1)) {
			flow = stableStateFunction(currentDepth + getSnapshotDepth(x, y - 1)) - getSnapshotDepth(x, y - 1);
			if (flow > MIN_FLOW) {
				// Leads to smoother flow
				flow = flow * 0.5f;
			}
			flow = constrain(flow, 0f, min(MAX_FLOW, currentDepth));

			Fluid nextBelow = topography.getFluids().get(x, y - 1);
			if (nextBelow == null) {
				topography.getFluids().put(x, y - 1, topography.getFluids().get(x, y).sub(flow));
			} else {
				nextBelow.add(topography.getFluids().get(x, y).sub(flow));
			}
			currentDepth -= flow;
		}

		if (currentDepth <= 0f) {
			return;
		}

		// Left
		if (isFlowable(x - 1, y)) {
			// Equalise the amount of water in this block and its neighbour
			flow = (getSnapshotDepth(x, y) - getSnapshotDepth(x - 1, y)) / 4f;
			if (flow > MIN_FLOW) {
				flow = flow * 0.5f;
			}
			flow = constrain(flow, 0f, currentDepth);

			Fluid nextLeft = topography.getFluids().get(x - 1, y);
			if (nextLeft == null) {
				topography.getFluids().put(x - 1, y, topography.getFluids().get(x, y).sub(flow));
			} else {
				nextLeft.add(topography.getFluids().get(x, y).sub(flow));
			}
			currentDepth -= flow;
		}

		if (currentDepth <= 0f) {
			return;
		}

		// Right
		if (isFlowable(x + 1, y)) {
			// Equalise the amount of water in this block and it's
			// neighbour
			flow = (getSnapshotDepth(x, y) - getSnapshotDepth(x + 1, y)) / 4f;
			if (flow > MIN_FLOW) {
				flow = flow * 0.5f;
			}
			flow = constrain(flow, 0f, currentDepth);

			Fluid nextRight = topography.getFluids().get(x + 1, y);
			if (nextRight == null) {
				topography.getFluids().put(x + 1, y, topography.getFluids().get(x, y).sub(flow));
			} else {
				nextRight.add(topography.getFluids().get(x, y).sub(flow));
			}
			currentDepth -= flow;
		}

		if (currentDepth <= 0f) {
			return;
		}

		// Up. Only compressed water flows upwards.
		if (isFlowable(x, y + 1)) {
			flow = currentDepth - stableStateFunction(currentDepth + getSnapshotDepth(x, y + 1));
			if (flow > MIN_FLOW) {
				flow = flow * 0.5f;
			}
			flow = constrain(flow, 0f, min(MAX_FLOW, currentDepth));

			Fluid nextAbove = topography.getFluids().get(x, y + 1);
			if (nextAbove == null) {
				topography.getFluids().put(x, y + 1, topography.getFluids().get(x, y).sub(flow));
			} else {
				nextAbove.add(topography.getFluids().get(x, y).sub(flow));
			}
			currentDepth -= flow;
		}
	}
	
	
	/**
	 * Calculates the stable state between two vertically adjacent fluids by their total mass
	 */
	private float stableStateFunction(float totalDepth) {
		if (totalDepth <= TILE_SIZE) {
			return TILE_SIZE;
		} else if (totalDepth < 2f * MAX_DEPTH + MAX_COMPRESSION) {
			return (MAX_DEPTH * MAX_DEPTH + totalDepth * MAX_COMPRESSION) / (MAX_DEPTH + MAX_COMPRESSION);
		} else {
			return (totalDepth + MAX_COMPRESSION) / 2f;
		}
	}
	
	
	/**
	 * Gets the depth of a fluid from the current frozen snapshot of the {@link FluidMap}
	 */
	private float getSnapshotDepth(int x, int y) {
		Fluid fluid = currentSnapshot.get(x, y);
		if (fluid == null) {
			return 0f;
		}
		
		return fluid.getDepth();
	}
	

	/**
	 * Constrains a float between boundaries
	 */
	private float constrain(float f, float lower, float upper) {
		if (f < lower) {
			return lower;
		} else if (f > upper) {
			return upper;
		} else {
			return f;
		}
	}
	

	/**
	 * @return true if tile at specified location is passable, indicating that fluid can flow through it.
	 */
	private boolean isFlowable(int x, int y) {
		try {
			return topography.getTile(x, y, true).isPassable();
		} catch (NullPointerException e) {
			return false;
		}
	}
}