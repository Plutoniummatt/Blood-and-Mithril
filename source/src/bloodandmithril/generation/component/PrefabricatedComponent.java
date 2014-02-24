package bloodandmithril.generation.component;

import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A Pre-Fabricated {@link Component} that does not require generation.
 *
 * @author Matt
 */
public abstract class PrefabricatedComponent extends Component {
	private static final long serialVersionUID = 1029361927219136813L;

	private final ComponentBlueprint blueprint;

	private final boolean inverted;

	/**
	 * Constructor
	 */
	protected PrefabricatedComponent(ComponentBlueprint blueprint, Boundaries boundaries, int structureKey, boolean inverted) {
		super(boundaries, structureKey);
		this.blueprint = blueprint;
		this.inverted = inverted;
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if (inverted) {
			return blueprint.getForegroundTile(
				boundaries.right - worldTileX,
				worldTileY - boundaries.bottom
			);
		} else {
			return blueprint.getForegroundTile(
				worldTileX - boundaries.left,
				worldTileY - boundaries.bottom
			);
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if (inverted) {
			return blueprint.getBackgroundTile(
				boundaries.right - worldTileX,
				worldTileY - boundaries.bottom
			);
		} else {
			return blueprint.getBackgroundTile(
				worldTileX - boundaries.left,
				worldTileY - boundaries.bottom
			);
		}
	}


	/**
	 * The blueprint for a {@link PrefabricatedComponent}.
	 *
	 * @author Matt
	 */
	public static class ComponentBlueprint {
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
}