package bloodandmithril.generation.component.components.prefab;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.generation.component.components.Stairs;
import bloodandmithril.generation.component.components.Stairs.StairsCreationCustomization;
import bloodandmithril.generation.component.interfaces.Interface;
import bloodandmithril.generation.component.interfaces.RectangularInterface.RectangularInterfaceCustomization;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickFloor;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;

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
	public UndergroundDesertTempleEntrance(int worldX, int worldY, int structureKey, boolean inverted, Class<? extends Tile> wallTile, Class<? extends Tile> backgroundTile) {
		super(
			blueprint(backgroundTile, wallTile),
			boundaries(worldX, worldY),
			structureKey,
			inverted
		);

		generateInterfaces();
	}


	private static Boundaries boundaries(int worldX, int worldY) {
		return new Boundaries(worldY, worldY - height - 1, worldX, worldX + width - 1);
	}


	private static ComponentBlueprint blueprint(Class<? extends Tile> backgroundTile, Class<? extends Tile> wallTile) {

		Tile[][] fTiles = new Tile[width][height];
		Tile[][] bTiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int fPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y);
				int bPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y + height);

				try {
					if (fPixel == Color.rgba8888(Color.RED)) {
						fTiles[x][height - 1 - y] = wallTile.newInstance();
					} else if (fPixel == Color.rgba8888(Color.WHITE)) {
						fTiles[x][height - 1 - y] = new Tile.EmptyTile();
					} else if (fPixel == Color.rgba8888(Color.BLUE)) {
						fTiles[x][height - 1 - y] = new YellowBrickPlatform();
					} else if (fPixel == Color.rgba8888(Color.GREEN)) {
						fTiles[x][height - 1 - y] = new YellowBrickFloor();
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


	@Override
	protected void generateInterfaces() {
		// Generate the bottom interface
		if (inverted) {
			generateUnitThicknessHorizontalInterfaces(boundaries.top - 101, boundaries.left + 212, boundaries.left + 228);
		} else {
			generateUnitThicknessHorizontalInterfaces(boundaries.top - 101, boundaries.right - 228, boundaries.right - 212);
		}
	}


	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		if (with.equals(Stairs.class)) {
			return stemStairs(custom);
		}

		return null;
	}


	/**
	 * Stem some {@link Stairs} from this component
	 */
	@SuppressWarnings("rawtypes")
	private Component stemStairs(ComponentCreationCustomization custom) {
		final StairsCreationCustomization stairsCustomization = (StairsCreationCustomization)custom;
		stairsCustomization.stemRight = this.inverted;
		stairsCustomization.slopeGradient = stairsCustomization.stemRight ? -stairsCustomization.slopeGradient : stairsCustomization.slopeGradient;

		// Create the connected interface from an available one, then create the component from the created interface
		Interface createdInterface = getAvailableInterfaces().get(0).createConnectedInterface(new RectangularInterfaceCustomization(1, stairsCustomization.corridorHeight - 1, 0, 0));
		Component createdComponent = createdInterface.createComponent(Stairs.class, stairsCustomization, getStructureKey());

		// Check for overlaps
		return checkForOverlaps(createdInterface, createdComponent);
	}
}