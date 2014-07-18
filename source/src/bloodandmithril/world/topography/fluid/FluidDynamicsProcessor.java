package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.min;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;
import bloodandmithril.world.topography.Topography;

/**
 * Class responsible for processing fluid dynamics.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class FluidDynamicsProcessor {

	/** The {@link Topography} instance this processor is responsible for */
	private Topography topography;

	/** The frozen snapshot of the current state of the {@link FluidMap} */
	private FluidMap currentSnapshot;

	/** Constants used */
	private static final float MAX_DEPTH = TILE_SIZE;
	private static final float MAX_COMPRESSION = TILE_SIZE;
	private static final float MAX_FLOW = TILE_SIZE;
	private static final float DIFFUSION_COEFFICIENT = 250f;

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

		currentSnapshot.getAllFluids().stream()
		.sorted((e1, e2) -> {
			return e1.y.compareTo(e2.y);
		})
		.forEach(entry -> {
			try {
				processFluid(entry);
			} catch (NullPointerException e) {
				Logger.generalDebug("NPE detected during fluid dynamics processing, probably concurrency related.", LogLevel.TRACE);
			}
		});
	}


	/**
	 * Process a single fluid
	 */
	private void processFluid(DualKeyEntry<Integer, Integer, Fluid> entry) {
		// synchronized (topography.getFluids()) {
		// 	diffuse(entry.x, entry.y, entry.value);
		// }

		flow(entry.x, entry.y, entry.value);
		if (entry.value.getDepth() < 0.05f) {
			topography.getFluids().remove(entry.x, entry.y);
		}
	}


	/**
	 * Handles diffusion.
	 *
	 * This this unused, as it causes performance problems, not in itself, but it disallows the "freezing" of fluids
	 */
	@SuppressWarnings("unused")
	private void diffuse(Integer x, Integer y, Fluid fluid) {
		// Diffusion downward
		Fluid down = topography.getFluids().get(x, y - 1);
		if (down != null) {
			float exchangeAmount = min(fluid.getDepth() / DIFFUSION_COEFFICIENT, down.getDepth() / DIFFUSION_COEFFICIENT);

			topography.getFluids().get(x, y).add(topography.getFluids().get(x, y - 1).sub(exchangeAmount));
			topography.getFluids().get(x, y - 1).add(topography.getFluids().get(x, y).sub(exchangeAmount));
		}

		// Diffusion upward
		Fluid up = topography.getFluids().get(x, y + 1);
		if (up != null && up.getDepth() > 2f) {
			float exchangeAmount = min(fluid.getDepth() / DIFFUSION_COEFFICIENT, up.getDepth() / DIFFUSION_COEFFICIENT);
			topography.getFluids().get(x, y).add(topography.getFluids().get(x, y + 1).sub(exchangeAmount));
			topography.getFluids().get(x, y + 1).add(topography.getFluids().get(x, y).sub(exchangeAmount));
		}

		// Diffusion sideways
		Fluid left = topography.getFluids().get(x - 1, y);
		Fluid right = topography.getFluids().get(x + 1, y);
		if (left != null && right != null) {
			float exchangeAmount = min(min(fluid.getDepth() / DIFFUSION_COEFFICIENT, left.getDepth() / DIFFUSION_COEFFICIENT), right.getDepth()/DIFFUSION_COEFFICIENT);

			if (Util.getRandom().nextBoolean()) {
				topography.getFluids().get(x, y).add(topography.getFluids().get(x - 1, y).sub(exchangeAmount));
				topography.getFluids().get(x - 1, y).add(topography.getFluids().get(x, y).sub(exchangeAmount));

				topography.getFluids().get(x, y).add(topography.getFluids().get(x + 1, y).sub(exchangeAmount));
				topography.getFluids().get(x + 1, y).add(topography.getFluids().get(x, y).sub(exchangeAmount));
			} else {
				topography.getFluids().get(x, y).add(topography.getFluids().get(x + 1, y).sub(exchangeAmount));
				topography.getFluids().get(x + 1, y).add(topography.getFluids().get(x, y).sub(exchangeAmount));

				topography.getFluids().get(x, y).add(topography.getFluids().get(x - 1, y).sub(exchangeAmount));
				topography.getFluids().get(x - 1, y).add(topography.getFluids().get(x, y).sub(exchangeAmount));
			}
		} else if (left != null) {
			float exchangeAmount = min(fluid.getDepth() / DIFFUSION_COEFFICIENT, left.getDepth() / DIFFUSION_COEFFICIENT);
			topography.getFluids().get(x, y).add(topography.getFluids().get(x - 1, y).sub(exchangeAmount));
			topography.getFluids().get(x - 1, y).add(topography.getFluids().get(x, y).sub(exchangeAmount));
		} else if (right != null) {
			float exchangeAmount = min(fluid.getDepth() / DIFFUSION_COEFFICIENT, right.getDepth() / DIFFUSION_COEFFICIENT);
			topography.getFluids().get(x, y).add(topography.getFluids().get(x + 1, y).sub(exchangeAmount));
			topography.getFluids().get(x + 1, y).add(topography.getFluids().get(x, y).sub(exchangeAmount));
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