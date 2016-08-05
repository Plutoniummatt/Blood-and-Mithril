package bloodandmithril.generation.component;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.world.topography.tile.Tile;

/**
 * The blueprint for a {@link PrefabricatedComponent}.
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class ComponentBlueprint implements Serializable {
	private static final long serialVersionUID = -5603987012800685781L;

	private final Tile[][] fTiles;
	private final Tile[][] bTiles;

	/**
	 * Constructor
	 */
	public ComponentBlueprint(Tile[][] fTiles, Tile[][] bTiles) {
		this.fTiles = fTiles;
		this.bTiles = bTiles;
	}


	public Tile getForegroundTile(int x, int y) {
		try {
			return fTiles[x][y];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}


	public Tile getBackgroundTile(int x, int y) {
		try {
			return bTiles[x][y];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
}