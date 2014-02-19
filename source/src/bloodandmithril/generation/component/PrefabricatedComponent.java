package bloodandmithril.generation.component;

import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A Pre-Fabricated {@link Component} that does not require generation.
 *
 * @author Matt
 */
public abstract class PrefabricatedComponent extends Component {

	private final ComponentBlueprint blueprint;

	/**
	 * Constructor
	 */
	protected PrefabricatedComponent(ComponentBlueprint blueprint, Boundaries boundaries, int structureKey) {
		super(boundaries, structureKey);
		this.blueprint = blueprint;
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		// TODO Auto-generated method stub
		return null;
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


		public Tile getForegroundTile() {
			// TODO
			return null;
		}


		public Tile getBackgroundTile() {
			// TODO
			return null;
		}
	}
}