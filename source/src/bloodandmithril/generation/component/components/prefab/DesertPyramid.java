package bloodandmithril.generation.component.components.prefab;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;

/**
 * An implementations of {@link PrefabricatedComponent}s that is a representation of a pyramid
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DesertPyramid extends PrefabricatedComponent {
	private static final long serialVersionUID = -6680438316954480488L;

	private static final int width = 208, height = 104;

	/**
	 * Constructor
	 */
	public DesertPyramid(int worldX, int worldY, int structureKey, Class<? extends Tile> wallTile, Class<? extends Tile> backgroundTile) {
		super(
			blueprint(backgroundTile, wallTile),
			boundaries(worldX, worldY),
			structureKey,
			false
		);
	}
	

	@Override
	protected void generateInterfaces() {
	}

	
	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		return null;
	}
	
	
	private static Boundaries boundaries(int worldX, int worldY) {
		return new Boundaries(worldY, worldY - height - 1, worldX, worldX + width - 1);
	}
	
	
	private static ComponentBlueprint blueprint(Class<? extends Tile> backgroundTile, Class<? extends Tile> wallTile) {

		Tile[][] fTiles = new Tile[width][height];
		Tile[][] bTiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int fPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y + 200);
				int bPixel = PrefabricatedComponent.prefabPixmap.getPixel(x + width, y + 200);

				try {
					if (fPixel == Color.rgba8888(Color.RED)) {
						fTiles[x][height - 1 - y] = wallTile.newInstance();
					} else if (fPixel == Color.rgba8888(Color.WHITE)) {
						fTiles[x][height - 1 - y] = new Tile.EmptyTile();
					} else if (fPixel == Color.rgba8888(Color.BLUE)) {
						fTiles[x][height - 1 - y] = new YellowBrickPlatform();
					} else if (fPixel == Color.rgba8888(Color.MAGENTA)) {
						Tile tile = wallTile.newInstance();
						fTiles[x][height - 1 - y] = tile;
					} else if (fPixel == Color.rgba8888(Color.BLACK)) {
						Tile tile = wallTile.newInstance();
						tile.changeToSmoothCeiling();
						fTiles[x][height - 1 - y] = tile;
					} else {
						fTiles[x][height - 1 - y] = null;
					}

					if (bPixel == Color.rgba8888(Color.BLACK)) {
						bTiles[x][height - 1 - y] = backgroundTile.newInstance();
					} else if (bPixel == Color.rgba8888(Color.RED)) {
						Tile tile = backgroundTile.newInstance();
						tile.changeToSmoothCeiling();
						bTiles[x][height - 1 - y] = tile;
					} else if (bPixel == Color.rgba8888(Color.WHITE)) {
						bTiles[x][height - 1 - y] = new Tile.EmptyTile();
					} else {
						bTiles[x][height - 1 - y] = null;
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return new ComponentBlueprint(fTiles, bTiles);
	}
}
