package bloodandmithril.world.topography.fluid;

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
			processSingleFluid(entry);
		});
	}


	private void processSingleFluid(DualKeyEntry<Integer, Integer, Fluid> entry) {
		Fluid below = getFluid(entry.x, entry.y - 1);
		if (below == null) {
			if (isFlowable(entry.x, entry.y - 1)) {
				topography.getFluids().put(entry.x, entry.y - 1, topography.getFluids().remove(entry.x, entry.y));
			} else {
				spread(entry);
			}
		} else {
			if (entry.value.sub(below.add(entry.value).getDepth()).getDepth() < 4f) {
				spread(entry);
			} else {
				if (entry.value.getDepth() == 0) {
					topography.getFluids().remove(entry.x, entry.y);
				}
			}
		}
	}
	

	private void spread(DualKeyEntry<Integer, Integer, Fluid> entry) {
		if (entry.value.getDepth() < 1) {
			return;
		}
		
		Fluid left = getFluid(entry.x - 1, entry.y);
		Fluid right = getFluid(entry.x + 1, entry.y);
		
		if (left == null) {
			if (isFlowable(entry.x - 1, entry.y)) {
				Fluid sub = entry.value.sub(entry.value.getDepth() / 2);
				if (sub.getDepth() != 0) {
					topography.getFluids().put(entry.x - 1, entry.y, sub);
				}
			}
		} else {
			if (left.getDepth() < entry.value.getDepth()) {
				float diff = entry.value.getDepth() - left.getDepth();
				left.add(entry.value.sub(diff/2));
			}
		}
		
		if (right == null) {
			if (isFlowable(entry.x + 1, entry.y)) {
				Fluid sub = entry.value.sub(entry.value.getDepth() / 2);
				if (sub.getDepth() != 0) {
					topography.getFluids().put(entry.x + 1, entry.y, sub);
				}
			}
		} else {
			if (right.getDepth() < entry.value.getDepth()) {
				float diff = entry.value.getDepth() - right.getDepth();
				right.add(entry.value.sub(diff/2));
			}
		}
	}


	private boolean isFlowable(int x, int y) {
		return topography.getTile(x, y, true).isPassable();
	}
	
	
	private Fluid getFluid(int x, int y) {
		return topography.getFluids().get(x, y);
	}
}