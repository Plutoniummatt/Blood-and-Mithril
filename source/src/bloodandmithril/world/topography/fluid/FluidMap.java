package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

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
	
	
	public synchronized Fluid get(int x, int y) {
		return fluids.get(x, y);
	}
	
	
	public Fluid put(int x, int y, Fluid v) {
		return fluids.put(x, y, v);
	}
	
	
	public Fluid put(float worldX, float worldY, Fluid v) {
		return fluids.put(
			convertToWorldTileCoord(worldX), 
			convertToWorldTileCoord(worldY), 
			v
		);
	}
	
	
	public synchronized Fluid remove(int x, int y) {
		return fluids.remove(x, y);
	}
	
	
	/**
	 * @return All fluids
	 */
	public List<DualKeyEntry<Integer, Integer, Fluid>> getAllFluids() {
		return fluids.getAllEntries();
	}
	
	
	public ConcurrentDualKeySkipListMap<Integer, Integer, Fluid> getData() {
		return fluids;
	}
	
	
	public FluidMap deepCopy() {
		FluidMap map = new FluidMap();
		for (DualKeyEntry<Integer, Integer, Fluid> entry : getAllFluids()) {
			map.put(entry.x, entry.y, entry.value.copy());
		}
		
		return map;
	}
}