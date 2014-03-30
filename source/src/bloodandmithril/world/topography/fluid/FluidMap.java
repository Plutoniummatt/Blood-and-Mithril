package bloodandmithril.world.topography.fluid;

import java.io.Serializable;
import java.util.List;

import bloodandmithril.util.datastructure.ConcurrentDualKeySkipListMap;
import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;

/**
 * Datastructure representing fluids
 *
 * @author Matt
 */
public class FluidMap implements Serializable {
	private static final long serialVersionUID = -4729159035178758411L;
	
	/** {@link Fluid}s, y-x coordinate, reversed for processing purposes */
	private final ConcurrentDualKeySkipListMap<Integer, Integer, Fluid> fluids = new ConcurrentDualKeySkipListMap<>();
	
	/**
	 * Constructor
	 */
	public FluidMap() {
	}
	
	
	public Fluid get(int x, int y) {
		return fluids.get(x, y);
	}
	
	
	public Fluid put(int x, int y, Fluid v) {
		return fluids.put(x, y, v);
	}
	
	
	public Fluid remove(int x, int y) {
		return fluids.remove(x, y);
	}
	
	
	/**
	 * @return All fluids
	 */
	public List<DualKeyEntry<Integer, Integer, Fluid>> getAllFluids() {
		return fluids.getAllEntries();
	}
}