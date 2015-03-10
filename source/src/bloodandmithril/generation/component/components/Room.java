package bloodandmithril.generation.component.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.components.Stairs.StairsCreationCustomization;
import bloodandmithril.generation.component.interfaces.Interface;
import bloodandmithril.generation.component.interfaces.RectangularInterface;
import bloodandmithril.generation.component.interfaces.RectangularInterface.RectangularInterfaceCustomization;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.tiles.brick.YellowBrickTile;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * A {@link Room}, with outer {@link Boundaries} and an inner {@link Boundaries}.  The region in between is the wall.
 *
 * Interfaces are generated on all edges that are free of existing interfaces
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Room extends Component {
	private static final long serialVersionUID = -4828689126496857514L;

	/** The inner boundaries that defines the cavity of the room */
	private final Boundaries innerBoundaries;

	/** Tile type of the walls of this room */
	private final Class<? extends Tile> wallTile = YellowBrickTile.class;

	/**
	 * Constructor
	 */
	public Room(Boundaries outerBoundaries, Boundaries innerBoundaries, int structureKey) {
		super(outerBoundaries, structureKey);
		this.innerBoundaries = innerBoundaries;
	}


	@Override
	protected void generateInterfaces() {

		generateUnitThicknessVerticalInterfaces(innerBoundaries.left, innerBoundaries.top, innerBoundaries.bottom);

		generateUnitThicknessVerticalInterfaces(innerBoundaries.right, innerBoundaries.top, innerBoundaries.bottom);

		generateUnitThicknessHorizontalInterfaces(innerBoundaries.top, innerBoundaries.left, innerBoundaries.right);

		generateUnitThicknessHorizontalInterfaces(innerBoundaries.bottom, innerBoundaries.left, innerBoundaries.right);
	}




	@Override
	protected <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom) {

		if (with.equals(Corridor.class)) {
			return stemCorridor(custom);
		}

		if (with.equals(Stairs.class)) {
			return stemStairs(custom);
		}

		return null;
	}


	/**
	 * Stem some {@link Stairs} from this {@link Room}
	 */
	@SuppressWarnings("rawtypes")
	private Component stemStairs(ComponentCreationCustomization custom) {
		final StairsCreationCustomization stairsCustomization = (StairsCreationCustomization)custom;

		// Filter out the top interfaces.
		// If we're stemming right, filter out left interfaces, if stemming left, filter out right interfaces
		// If we're stemming up, filter out bottom
		Collection<Interface> interfacesToUse = Collections2.filter(getAvailableInterfaces(), new Predicate<Interface>() {
			@Override
			public boolean apply(Interface input) {
				if (input instanceof RectangularInterface) {
					RectangularInterface iface = (RectangularInterface) input;

					boolean isNotTopInterface = !(iface.boundaries.bottom == innerBoundaries.top);
					boolean isNotWrongSide = stairsCustomization.stemRight ? !(iface.boundaries.right == innerBoundaries.left) : !(iface.boundaries.left == innerBoundaries.right);
					boolean isNotBottom = stairsCustomization.stemRight && stairsCustomization.slopeGradient > 0 || !stairsCustomization.stemRight && stairsCustomization.slopeGradient < 0 ? !(iface.boundaries.top == innerBoundaries.bottom) : true;

					return isNotTopInterface && isNotWrongSide && isNotBottom;
				} else {
					throw new RuntimeException();
				}
			}
		});

		// Convert filtered collection into ArrayList, select an interface at random
		if (interfacesToUse.size() == 0) {
			return null;
		}

		int interfaceIndex = Util.getRandom().nextInt(interfacesToUse.size());
		List<Interface> interfacesToUseList = new ArrayList<>(interfacesToUse);

		// Create the connected interface from an available one, then create the component from the created interface
		Interface createdInterface;
		Component createdComponent;

		// Vertical interface
		if (((RectangularInterface)interfacesToUseList.get(interfaceIndex)).boundaries.left == ((RectangularInterface)interfacesToUseList.get(interfaceIndex)).boundaries.right) {
			createdInterface = interfacesToUseList.get(interfaceIndex).createConnectedInterface(new RectangularInterfaceCustomization(stairsCustomization.corridorHeight - 1, 1, 0, 0));
			createdComponent = createdInterface.createComponent(Stairs.class, stairsCustomization, getStructureKey());

		// Bottom interface
		} else {
			createdInterface = interfacesToUseList.get(interfaceIndex).createConnectedInterface(new RectangularInterfaceCustomization(1, stairsCustomization.corridorHeight - 1, 0, 0));
			createdComponent = createdInterface.createComponent(Stairs.class, stairsCustomization, getStructureKey());
		}

		// Check for overlaps
		return checkForOverlaps(createdInterface, createdComponent);
	}


	/**
	 * @return Stem a {@link Corridor} from this {@link Room}
	 */
	@SuppressWarnings("rawtypes")
	private Component stemCorridor(ComponentCreationCustomization custom) {
		CorridorCreationCustomization corridorCustomization = (CorridorCreationCustomization) custom;

		// Filter out any horizontal interfaces
		Collection<Interface> verticalInterfacesCollection = Collections2.filter(getAvailableInterfaces(), verticalInterfacePredicate);

		// Determine whether to use left or right interfaces
		if (corridorCustomization.stemRight) {
			verticalInterfacesCollection = Collections2.filter(getAvailableInterfaces(), new Predicate<Interface>() {
				@Override
				public boolean apply(Interface input) {
					if (input instanceof RectangularInterface) {
						RectangularInterface iface = (RectangularInterface) input;
						return iface.boundaries.left == iface.boundaries.right && iface.boundaries.right == innerBoundaries.right;
					} else {
						throw new RuntimeException();
					}
				}
			});
		} else {
			verticalInterfacesCollection = Collections2.filter(getAvailableInterfaces(), new Predicate<Interface>() {
				@Override
				public boolean apply(Interface input) {
					if (input instanceof RectangularInterface) {
						RectangularInterface iface = (RectangularInterface) input;
						return iface.boundaries.left == iface.boundaries.right && iface.boundaries.left == innerBoundaries.left;
					} else {
						throw new RuntimeException();
					}
				}
			});
		}

		// Convert filtered collection into ArrayList, select an interface at random
		List<Interface> verticalInterfacesList = new ArrayList<>(verticalInterfacesCollection);
		int interfaceIndex = Util.getRandom().nextInt(verticalInterfacesList.size());

		// Create the connected interface from an available one, then create the component from the created interface
		Interface createdInterface = verticalInterfacesList.get(interfaceIndex).createConnectedInterface(new RectangularInterfaceCustomization(corridorCustomization.height - 1, 1, 0, 0));
		Component createdComponent = createdInterface.createComponent(Corridor.class, corridorCustomization, getStructureKey());

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
					return wallTile.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} else {
			return null;
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if (getBoundaries().isWithin(worldTileX, worldTileY)) {
			try {
				return wallTile.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}


	/**
	 * Customization values used when creating a {@link Room} via an {@link Interface}
	 *
	 * @author Sam
	 */
	public static class RoomCreationCustomization extends ComponentCreationCustomization<Room> {

		public boolean stemRight;
		public int length, height, wallThickness;
		Class<? extends Tile> tileType;

		/**
		 * Constructor
		 */
		public RoomCreationCustomization(boolean stemRight, int length, int height, int wallThickness, Class<? extends Tile> tileType) {
			this.stemRight = stemRight;
			this.length = length;
			this.height = height;
			this.wallThickness = wallThickness;
			this.tileType = tileType;
		}
	}
}