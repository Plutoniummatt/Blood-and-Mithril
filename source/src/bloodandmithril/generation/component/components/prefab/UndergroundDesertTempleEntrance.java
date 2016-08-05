package bloodandmithril.generation.component.components.prefab;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.ComponentBlueprint;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.generation.component.components.stemming.interfaces.HorizontalInterface;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.wood.WoodenPlatform;

/**
 * An implementations of {@link PrefabricatedComponent}s that is a representation of an entrance to underground temple-like annexes
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class UndergroundDesertTempleEntrance extends PrefabricatedComponent {
	private static final long serialVersionUID = 4185881549137827481L;

	private static final int width = 387, height = 100;

	/**
	 * Constructor
	 */
	public UndergroundDesertTempleEntrance(final int worldX, final int worldY, final int structureKey, final boolean inverted, final Class<? extends Tile> wallTile, final Class<? extends Tile> backgroundTile) {
		super(
			blueprint(backgroundTile, wallTile),
			boundaries(worldX, worldY),
			structureKey,
			inverted
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

				final int fPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y);
				final int bPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y + height);

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
		addInterface(new HorizontalInterface(
			boundaries.left + 158, 
			boundaries.bottom, 
			17
		));
	}
}