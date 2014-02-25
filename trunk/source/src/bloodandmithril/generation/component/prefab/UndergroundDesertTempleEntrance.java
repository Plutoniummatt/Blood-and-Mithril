package bloodandmithril.generation.component.prefab;

import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;


/**
 * An implementations of {@link PrefabricatedComponent}s that is a representation of an entrance to underground temple-like annexes
 *
 * @author Matt
 */
public class UndergroundDesertTempleEntrance extends PrefabricatedComponent {

	private final Class<? extends Tile> backgroundTile;
	private final Class<? extends Tile> wallTile;

	/**
	 * Constructor
	 */
	public UndergroundDesertTempleEntrance(int worldX, int worldY, int structureKey, boolean inverted, Class<? extends Tile> wallTile, Class<? extends Tile> backgroundTile) {
		super(
			blueprint(),
			boundaries(worldX, worldY),
			structureKey,
			inverted
		);

		this.wallTile = wallTile;
		this.backgroundTile = backgroundTile;
	}


	private static ComponentBlueprint blueprint() {
		Tile[][] fTiles = new Tile[140][40];
		Tile[][] bTiles = new Tile[140][40];

		return new ComponentBlueprint(fTiles, bTiles);
	}


	private static Boundaries boundaries(int worldX, int worldY) {
		return new Boundaries(worldY, worldY - 39, worldX, worldX + 139);
	}


	@Override
	protected void generateInterfaces() {
		// TODO Auto-generated method stub
	}


	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		// TODO Auto-generated method stub
		return null;
	}
}