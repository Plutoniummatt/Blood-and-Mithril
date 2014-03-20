package bloodandmithril.world.topography;

import static bloodandmithril.world.topography.Topography.convertToWorldCoord;
import bloodandmithril.util.datastructure.DualKeyHashMap;
import bloodandmithril.util.datastructure.DualKeyHashMap.DualKeyEntry;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;
import bloodandmithril.world.topography.tile.tiles.FluidTile;

/**
 * Class for processing fluid dynamics.
 *
 * @author Matt
 */
public class FluidDynamicsProcessor {

	/**
	 * The {@link Topography} instance that this {@link FluidDynamicsProcessor} is responsible for.
	 */
	private Topography topography;
	
	
	/**
	 * All the fluid tiles that exist on a given topography.
	 */
	private DualKeyHashMap<Integer, Integer, FluidTile> fluidTiles = new DualKeyHashMap<>();
	
	
	/**
	 * Constructor
	 */
	public FluidDynamicsProcessor(Topography topography) {
		this.topography = topography;
	}
	
	
	/**
	 * Processes a {@link FluidTile}, given the {@link Topography} and world tile coordinates.
	 */
	public void process() {
		for (DualKeyEntry<Integer, Integer, ? extends FluidTile> entry : fluidTiles.getAllEntries()) {
			processTile(entry.x, entry.y, entry.value);
		}
	}
	
	
	/**
	 * Processes a single tile
	 */
	private void processTile(int x, int y, FluidTile tile) {
		calculateAdjascent(x - 1, y, tile);
		calculateAdjascent(x + 1, y, tile);
		calculateAdjascent(x, y - 1, tile);
		calculateAdjascent(x, y + 1, tile);
	}


	/**
	 * Calculates fluid dynamics for a tile adjacent to a fluid tile.
	 */
	private void calculateAdjascent(int x, int y, FluidTile toProcess) {
		Tile adjacent = getTile(x, y);
		if (adjacent instanceof EmptyTile) {
			try {
				FluidTile newTile = toProcess.getClass().newInstance();
				newTile.setDepth(toProcess.getDepth() / 2);
				topography.changeTile(convertToWorldCoord(x, false), convertToWorldCoord(y, false), true, newTile);
				fluidTiles.put(x, y, newTile);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if (adjacent instanceof FluidTile) {
			((FluidTile) adjacent).setDepth(
				(((FluidTile) adjacent).getDepth() + toProcess.getDepth()) / 2
			);
		}
	}


	/**
	 * @return Does what it says on the label.
	 */
	private Tile getTile(int x, int y) {
		return topography.getTile(
			convertToWorldCoord(x + 1, false),
			convertToWorldCoord(y, false),
			true
		);
	}
}