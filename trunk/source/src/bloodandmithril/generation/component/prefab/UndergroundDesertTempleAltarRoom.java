package bloodandmithril.generation.component.prefab;

import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;

import com.badlogic.gdx.graphics.Color;

/**
 * Alter room, part of the underground desert temple series
 *
 * @author Matt
 */
public class UndergroundDesertTempleAltarRoom extends PrefabricatedComponent {
	private static final long serialVersionUID = -864803667393133353L;

	public UndergroundDesertTempleAltarRoom(int worldX, int worldY, int structureKey, boolean inverted, Class<? extends Tile> wallTile, Class<? extends Tile> backgroundTile) {
		super(
			blueprint(backgroundTile, wallTile),
			boundaries(worldX, worldY),
			structureKey,
			inverted
		);
	}


	private static Boundaries boundaries(int worldX, int worldY) {
		return new Boundaries(worldY, worldY - 39, worldX - 84, worldX);
	}


	private static ComponentBlueprint blueprint(Class<? extends Tile> backgroundTile, Class<? extends Tile> wallTile) {
		Tile[][] fTiles = new Tile[85][40];
		Tile[][] bTiles = new Tile[85][40];

		for (int x = 0; x < 85; x++) {
			for (int y = 0; y < 40; y++) {

				int fPixel = PrefabricatedComponent.prefabPixmap.getPixel(x + 100, y);
				int bPixel = PrefabricatedComponent.prefabPixmap.getPixel(x + 100, y + 40);

				try {
					if (fPixel == Color.rgba8888(Color.RED)) {
						fTiles[x][39 - y] = wallTile.newInstance();
					} else if (fPixel == Color.rgba8888(Color.WHITE)) {
						fTiles[x][39 - y] = new Tile.EmptyTile();
					} else if (fPixel == Color.rgba8888(Color.BLUE)) {
						fTiles[x][39 - y] = new YellowBrickPlatform();
					} else {
						fTiles[x][39 - y] = null;
					}

					if (bPixel == Color.rgba8888(Color.BLACK)) {
						bTiles[x][39 - y] = backgroundTile.newInstance();
					} else {
						bTiles[x][39 - y] = null;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return new ComponentBlueprint(fTiles, bTiles);
	}


	@Override
	protected void generateInterfaces() {
		// Generate the side interface
		if (inverted) {
			generateUnitThicknessVerticalInterfaces(boundaries.right, boundaries.top - 22, boundaries.top - 27);
		} else {
			generateUnitThicknessVerticalInterfaces(boundaries.left, boundaries.top - 22, boundaries.top - 27);
		}
	}


	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		return null;
	}


	public static class UndergroundDesertTempleAltarRoomCustomization extends ComponentCreationCustomization<UndergroundDesertTempleAltarRoom> {

		public final boolean inverted;
		public final Class<? extends Tile> wallTile;
		public final Class<? extends Tile> backgroundTile;

		/**
		 * Constructor
		 */
		public UndergroundDesertTempleAltarRoomCustomization(boolean inverted, Class<? extends Tile> wallTile, Class<? extends Tile> backgroundTile) {
			this.inverted = inverted;
			this.wallTile = wallTile;
			this.backgroundTile = backgroundTile;
		}
	}
}