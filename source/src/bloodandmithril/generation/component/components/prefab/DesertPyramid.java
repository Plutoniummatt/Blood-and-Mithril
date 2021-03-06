package bloodandmithril.generation.component.components.prefab;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.ComponentBlueprint;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.wood.WoodenPlatform;

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
	public DesertPyramid(final int worldX, final int worldY, final int structureKey, final Class<? extends Tile> wallTile, final Class<? extends Tile> backgroundTile) {
		super(
			blueprint(backgroundTile, wallTile),
			boundaries(worldX, worldY),
			structureKey,
			false
		);
	}


	private static Boundaries boundaries(final int worldX, final int worldY) {
		return new Boundaries(worldY, worldY - height - 1, worldX, worldX + width - 1);
	}


	private static ComponentBlueprint blueprint(final Class<? extends Tile> backgroundTile, final Class<? extends Tile> wallTile) {

		final Tile[][] fTiles = new Tile[width][height];
		final Tile[][] bTiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				final int fPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y + 200);
				final int bPixel = PrefabricatedComponent.prefabPixmap.getPixel(x + width, y + 200);

				try {
					if (fPixel == Color.rgba8888(Color.RED)) {
						fTiles[x][height - 1 - y] = wallTile.newInstance();
					} else if (fPixel == Color.rgba8888(Color.WHITE)) {
						fTiles[x][height - 1 - y] = new Tile.EmptyTile();
					} else if (fPixel == Color.rgba8888(Color.BLUE)) {
						fTiles[x][height - 1 - y] = new WoodenPlatform();
					} else if (fPixel == Color.rgba8888(Color.MAGENTA)) {
						final Tile tile = wallTile.newInstance();
						fTiles[x][height - 1 - y] = tile;
					} else if (fPixel == Color.rgba8888(Color.BLACK)) {
						final Tile tile = wallTile.newInstance();
						tile.changeToSmoothCeiling();
						fTiles[x][height - 1 - y] = tile;
					} else {
						fTiles[x][height - 1 - y] = null;
					}

					if (bPixel == Color.rgba8888(Color.BLACK)) {
						bTiles[x][height - 1 - y] = backgroundTile.newInstance();
					} else if (bPixel == Color.rgba8888(Color.RED)) {
						final Tile tile = backgroundTile.newInstance();
						tile.changeToSmoothCeiling();
						bTiles[x][height - 1 - y] = tile;
					} else if (bPixel == Color.rgba8888(Color.WHITE)) {
						bTiles[x][height - 1 - y] = new Tile.EmptyTile();
					} else {
						bTiles[x][height - 1 - y] = null;
					}
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return new ComponentBlueprint(fTiles, bTiles);
	}


	@Override
	public void generateInterfaces() {
	}
}
