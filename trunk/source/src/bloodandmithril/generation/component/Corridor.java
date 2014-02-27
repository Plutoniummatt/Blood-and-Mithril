package bloodandmithril.generation.component;

import bloodandmithril.generation.Structure;
import bloodandmithril.generation.component.RectangularInterface.RectangularInterfaceCustomization;
import bloodandmithril.generation.component.Room.RoomCreationCustomization;
import bloodandmithril.generation.component.Stairs.StairsCreationCustomization;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A Corridor, that may be used to connect other {@link Component}s
 *
 * Corridors are effectively just rectangles, single vertical 1-tile thick interfaces are generated on the
 * left and right edges of the rectangle, no walls are present on the interface edges,
 * but walls are present on both the top and bottom edges.
 *
 * @author Matt
 */
public class Corridor extends Component {
	private static final long serialVersionUID = 3194155344105601040L;

	/** Boundaries that define this corridor */
	private final Boundaries innerBoundaries;

	/** Tiles to construct this corridor from */
	private final Class<? extends Tile> tileType;

	/**
	 * Constructor
	 *
	 * @param innerBoundaries - The {@link Boundaries} that define the {@link Corridor}s inner walls.
	 * @param ceilingThickness - The thickness (number of tiles) of the ceiling of the {@link Corridor}.
	 * @param floorThickness - The thickness (number of tiles) of the floor of the {@link Corridor}.
	 * @param tileType - The class of {@link Tile} that the {@link Corridor} is to be constructed from.
	 * @param structureKey - The key of the {@link Structure} this {@link Corridor} exists on
	 */
	public Corridor(Boundaries innerBoundaries, int ceilingThickness, int floorThickness, Class<? extends Tile> tileType, int structureKey) {
		super(new Boundaries(innerBoundaries.top + ceilingThickness, innerBoundaries.bottom - floorThickness, innerBoundaries.left, innerBoundaries.right), structureKey);
		this.innerBoundaries = innerBoundaries;
		this.tileType = tileType;
	}


	@Override
	protected void generateInterfaces() {
		//Left interface
		boolean left = true;
		for (Interface iface : getExistingInterfaces()) {
			if(((RectangularInterface) iface).boundaries.left == innerBoundaries.left) {
				left = false;
			}
		}

		if (left) {
			getAvailableInterfaces().add(
				new RectangularInterface(new Boundaries(innerBoundaries.top, innerBoundaries.bottom, innerBoundaries.left, innerBoundaries.left))
			);
		}

		//Right interface
		boolean right = true;
		for (Interface iface : getExistingInterfaces()) {
			if(((RectangularInterface) iface).boundaries.right == innerBoundaries.right) {
				right = false;
			}
		}
		if (right) {
			getAvailableInterfaces().add(
				new RectangularInterface(new Boundaries(innerBoundaries.top, innerBoundaries.bottom, innerBoundaries.right, innerBoundaries.right))
			);
		}
	}


	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {

		if (with.equals(Room.class)) {
			return stemRoom(custom);
		}

		if (with.equals(Stairs.class)) {
			return stemStairs(custom);
		}

		return null;
	}


	/**
	 * Stem a set of {@link Stairs} from this {@link Corridor}
	 */
	@SuppressWarnings("rawtypes")
	private Component stemStairs(ComponentCreationCustomization custom) {
		StairsCreationCustomization stairsCustomization = (StairsCreationCustomization) custom;

		stairsCustomization.stemRight = innerBoundaries.left == ((RectangularInterface)getExistingInterfaces().get(0)).boundaries.left;

		// Create the connected interface from an available one, then create the component from the created interface
		Interface createdInterface = getAvailableInterfaces().get(0).createConnectedInterface(new RectangularInterfaceCustomization(innerBoundaries.top - innerBoundaries.bottom, 1, 0, 0));
		Component createdComponent = createdInterface.createComponent(Stairs.class, stairsCustomization, getStructureKey());

		// Check for overlaps
		return checkForOverlaps(createdInterface, createdComponent);
	}


	/**
	 * Stem a {@link Room} from this {@link Corridor}
	 */
	@SuppressWarnings("rawtypes")
	private Component stemRoom(ComponentCreationCustomization custom) {
		RoomCreationCustomization roomCustomization = (RoomCreationCustomization) custom;

		roomCustomization.stemRight = innerBoundaries.left == ((RectangularInterface)getExistingInterfaces().get(0)).boundaries.left;

		// Create the connected interface from an available one, then create the component from the created interface
		Interface createdInterface = getAvailableInterfaces().get(0).createConnectedInterface(new RectangularInterfaceCustomization(roomCustomization.height, 1, 0, 0));
		Component createdComponent = createdInterface.createComponent(Room.class, roomCustomization, getStructureKey());

		// Check for overlaps
		return checkForOverlaps(createdInterface, createdComponent);
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {

		if (getBoundaries().isWithin(worldTileX, worldTileY)) {

			if (innerBoundaries.isWithin(worldTileX, worldTileY)) {
				return new Tile.EmptyTile();
			} else {
				try {
					return tileType.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return null;
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {

		if (innerBoundaries.isWithin(worldTileX, worldTileY)) {
			try {
				return tileType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}


	/**
	 * Customization values used when creating a {@link Corridor} via an {@link Interface}
	 *
	 * @author Matt
	 */
	public static class CorridorCreationCustomization extends ComponentCreationCustomization<Corridor> {

		public boolean stemRight;

		public int ceilingThickness, floorThickness, length, height;

		public Class<? extends Tile> tileType;

		/**
		 * Constructor
		 */
		public CorridorCreationCustomization(boolean stemRight, int ceilingThickness, int floorThickness, int length, int height, Class<? extends Tile> tileType) {
			this.stemRight = stemRight;
			this.ceilingThickness = ceilingThickness;
			this.floorThickness = floorThickness;
			this.length = length;
			this.height = height;
			this.tileType = tileType;
		}
	}
}