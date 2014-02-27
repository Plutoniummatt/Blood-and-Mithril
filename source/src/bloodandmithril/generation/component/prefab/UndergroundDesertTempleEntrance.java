package bloodandmithril.generation.component.prefab;

import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.Corridor;
import bloodandmithril.generation.component.Interface;
import bloodandmithril.generation.component.PrefabricatedComponent;
import bloodandmithril.generation.component.RectangularInterface;
import bloodandmithril.generation.component.Room;
import bloodandmithril.generation.component.Stairs;
import bloodandmithril.generation.component.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.RectangularInterface.RectangularInterfaceCustomization;
import bloodandmithril.generation.component.Stairs.StairsCreationCustomization;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickPlatform;


/**
 * An implementations of {@link PrefabricatedComponent}s that is a representation of an entrance to underground temple-like annexes
 *
 * @author Matt
 */
public class UndergroundDesertTempleEntrance extends PrefabricatedComponent {
	private static final long serialVersionUID = 4185881549137827481L;
	
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


	private static ComponentBlueprint blueprint(Class<? extends Tile> backgroundTile, Class<? extends Tile> wallTile) {
		Tile[][] fTiles = new Tile[100][40];
		Tile[][] bTiles = new Tile[100][40];
		
		for (int x = 0; x < 100; x++) {
			for (int y = 0; y < 40; y++) {
				
				int fPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y);
				int bPixel = PrefabricatedComponent.prefabPixmap.getPixel(x, y + 40);
				
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
					bTiles[x][39 - y] = bPixel == Color.rgba8888(Color.BLACK) ? backgroundTile.newInstance() : null;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return new ComponentBlueprint(fTiles, bTiles);
	}


	private static Boundaries boundaries(int worldX, int worldY) {
		return new Boundaries(worldY, worldY - 39, worldX, worldX + 99);
	}


	@Override
	protected void generateInterfaces() {
		// Generate the bottom interface
		if (inverted) {
			getAvailableInterfaces().add(new RectangularInterface(new Boundaries(boundaries.top - 29, boundaries.top - 29, boundaries.right - 10, boundaries.right - 4)));
		} else {
			getAvailableInterfaces().add(new RectangularInterface(new Boundaries(boundaries.top - 29, boundaries.top - 29, boundaries.left + 4, boundaries.left + 10)));
		}
		
		// Generate the side interface
		if (inverted) {
			getAvailableInterfaces().add(new RectangularInterface(new Boundaries(boundaries.top - 17, boundaries.top - 22, boundaries.right, boundaries.right)));
		} else {
			getAvailableInterfaces().add(new RectangularInterface(new Boundaries(boundaries.top - 17, boundaries.top - 22, boundaries.left, boundaries.left)));
		}
	}


	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {
		if (with.equals(Stairs.class)) {
			return stemStairs(custom);
		}
		
		if (with.equals(Corridor.class)) {
			return stemCorridor(custom);
		}
		
		return null;
	}
	
	
	/**
	 * Stem a {@link Corridor} from this component
	 */
	@SuppressWarnings("rawtypes")
	private Component stemCorridor(ComponentCreationCustomization custom) {
		CorridorCreationCustomization corridorCustomization = (CorridorCreationCustomization) custom;

		// Filter out any horizontal interfaces
		Collection<Interface> verticalInterfacesCollection = Collections2.filter(getAvailableInterfaces(), verticalInterfacePredicate);

		if (!verticalInterfacesCollection.isEmpty()) {
			Interface createConnectedInterface = verticalInterfacesCollection.iterator().next().createConnectedInterface(
				new RectangularInterfaceCustomization(
					6, 
					1, 
					0, 
					0
				)
			);
			
			return checkForOverlaps(createConnectedInterface, createConnectedInterface.createComponent(Corridor.class, corridorCustomization, getStructureKey()));
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