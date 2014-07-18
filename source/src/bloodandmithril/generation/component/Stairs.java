package bloodandmithril.generation.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Corridor.CorridorCreationCustomization;
import bloodandmithril.generation.component.RectangularInterface.RectangularInterfaceCustomization;
import bloodandmithril.util.Util;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Stairs, these are sloped {@link Component}s similar to {@link Corridor}s....apart from these are well...sloped.
 *
 * @author Matt, Sam
 */
@Copyright("Matthew Peck 2014")
public class Stairs extends Component {
	private static final long serialVersionUID = -5878621523794581673L;

	/** y = mx + c values */
	private final int slopeConstant;
	private final float slopeGradient;

	/** Height of the space from step to ceiling above */
	private final int corridorHeight;

	/** Main tile used for the ceiling and borders on these {@link Stairs}. */
	private final Class<? extends Tile> tileType;

	/** Platform Tile used on the floor of these {@link Stairs} */
	private final Class<? extends Tile> stairType;

	/** How think the border on the ceiling and under the floor is */
	private final int borderThickness;


	/**
	 * @param slopeGradient - The gradient of the stairs
	 * @param slopeConstant - Stairs are along the line y = mx + c, this is c
	 * @param boundaries - The {@link Boundaries} bounding this entire section of {@link Stairs}.
	 * @param tileType - the {@link Tile} type used as the main block for these stairs
	 * @param stairType - the {@link Tile} type used as the steps on these stairs.
	 */
	public Stairs(float slopeGradient, int slopeConstant, int corridorHeight, int borderThickness, Boundaries boundaries, Class<? extends Tile> tileType, Class<? extends Tile> stairType, int structureKey) {
		super(boundaries, structureKey);
		this.slopeGradient = slopeGradient;
		this.slopeConstant = slopeConstant;
		this.corridorHeight = corridorHeight;
		this.borderThickness = borderThickness;
		this.tileType = tileType;
		this.stairType = stairType;
	}


	@Override
	public Tile getForegroundTile(int worldTileX, int worldTileY) {

		// If we're within the boundaries, carry on, otherwise return null
		if(getBoundaries().isWithin(worldTileX, worldTileY)) {

			// If we're on the stair line, create a stair tile and return it
			if(worldTileY == slopeGradient * worldTileX + slopeConstant) {
				try {
					Tile newInstance = stairType.newInstance();
					newInstance.changeToStair();
					return newInstance;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			// If we're between the lines defined as the cavity of the stairs, return empty
			} else if (
				worldTileY > slopeGradient * worldTileX + slopeConstant &&
				worldTileY <= slopeGradient * worldTileX + slopeConstant + corridorHeight
			) {
				return new Tile.EmptyTile();

			// If we're within the region outside the cavity but within the stairs, return main tile type
			} else if (
				worldTileY < slopeGradient * worldTileX + slopeConstant &&
				worldTileY >= slopeGradient * worldTileX + slopeConstant - borderThickness ||
				worldTileY > slopeGradient * worldTileX + slopeConstant + corridorHeight &&
				worldTileY <= slopeGradient * worldTileX + slopeConstant + corridorHeight + borderThickness
			) {
				try {
					Tile newInstance = tileType.newInstance();

					// But if we're also on the ceiling boundary, make it a smooth ceiling tile
					if (worldTileY == slopeGradient * worldTileX + slopeConstant + corridorHeight + 1) {
						newInstance.changeToSmoothCeiling();
					}
					return newInstance;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


	@Override
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if(getBoundaries().isWithin(worldTileX, worldTileY)) {
			if (
				worldTileY >= slopeGradient * worldTileX + slopeConstant - borderThickness &&
				worldTileY <= slopeGradient * worldTileX + slopeConstant + corridorHeight + borderThickness
			) {
				try {
					return tileType.newInstance();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}


	@Override
	protected void generateInterfaces() {
		generateVerticalInterfaces(getBoundaries().left);
		generateVerticalInterfaces(getBoundaries().right);

		generateHorizontalInterfaces(getBoundaries().top);
		generateHorizontalInterfaces(getBoundaries().bottom);
	}


	private void generateVerticalInterfaces(int x) {
		Integer top = null;
		for (int y = getBoundaries().top; y >= getBoundaries().bottom; y--) {
			Tile foregroundTile = getForegroundTile(x, y);
			if (foregroundTile != null) {
				if (foregroundTile instanceof EmptyTile) {
					if (top == null && doesNotIntersectWithExistingInterfaces(x, y)) {
						top = y;
					}
				} else {
					if (top != null) {
						getAvailableInterfaces().add(new RectangularInterface(new Boundaries(top, y + 1, x, x)));
						break;
					}
				}
			}
		}
	}


	private boolean doesNotIntersectWithExistingInterfaces(int x, int y) {
		for (Interface iface : getExistingInterfaces()) {
			if (((RectangularInterface) iface).boundaries.isWithin(x, y)) {
				return false;
			}
		}
		return true;
	}


	private void generateHorizontalInterfaces(int y) {
		Integer right = null;
		for (int x = getBoundaries().right; x >= getBoundaries().left; x--) {
			Tile foregroundTile = getForegroundTile(x, y);
			if (foregroundTile != null) {
				if (foregroundTile instanceof EmptyTile) {
					if (right == null && doesNotIntersectWithExistingInterfaces(x, y)) {
						right = x;
					}
				} else {
					if (right != null) {
						getAvailableInterfaces().add(new RectangularInterface(new Boundaries(y, y, x + 1, right)));
						break;
					}
				}
			}
		}
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


	@SuppressWarnings("rawtypes")
	private Component stemStairs(ComponentCreationCustomization custom) {
		final StairsCreationCustomization stairsCustomization = (StairsCreationCustomization)custom;

		// If we're stemming right, filter out left interfaces, if stemming left, filter out right interfaces
		Collection<Interface> interfacesToUse = Collections2.filter(getAvailableInterfaces(), new Predicate<Interface>() {
			@Override
			public boolean apply(Interface input) {
				if (input instanceof RectangularInterface) {
					RectangularInterface iface = (RectangularInterface) input;

					boolean isNotWrongVertical = stairsCustomization.stemRight ? !(iface.boundaries.right == getBoundaries().left) : !(iface.boundaries.left == getBoundaries().right);
					boolean isNotWrongHorizontal = stairsCustomization.stemRight == stairsCustomization.slopeGradient > 0 ? !(iface.boundaries.top == getBoundaries().bottom) : !(iface.boundaries.bottom == getBoundaries().top);

					return isNotWrongVertical && isNotWrongHorizontal;
				} else {
					throw new RuntimeException();
				}
			}
		});

		if (interfacesToUse.size() == 0) {
			return null;
		}

		// Convert filtered collection into ArrayList, select an interface at random
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
						return iface.boundaries.left == iface.boundaries.right && iface.boundaries.right == getBoundaries().right;
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
						return iface.boundaries.left == iface.boundaries.right && iface.boundaries.left == getBoundaries().left;
					} else {
						throw new RuntimeException();
					}
				}
			});
		}

		// Convert filtered collection into ArrayList, select an interface at random
		List<Interface> verticalInterfacesList = new ArrayList<>(verticalInterfacesCollection);
		if (verticalInterfacesList.size() == 0) {
			return null;
		}
		int interfaceIndex = Util.getRandom().nextInt(verticalInterfacesList.size());

		// Create the connected interface from an available one, then create the component from the created interface
		Interface createdInterface = verticalInterfacesList.get(interfaceIndex).createConnectedInterface(new RectangularInterfaceCustomization(corridorCustomization.height - 1, 1, 0, 0));
		Component createdComponent = createdInterface.createComponent(Corridor.class, corridorCustomization, getStructureKey());

		// Check for overlaps
		return checkForOverlaps(createdInterface, createdComponent);
	}


	/**
	 * Customization values used when creating a {@link Stairs} via an {@link Interface}
	 *
	 * @author Sam
	 */
	public static class StairsCreationCustomization extends ComponentCreationCustomization<Stairs> {

		public int length, reciprocalGradient, corridorHeight, borderThickness;

		public float slopeGradient;

		public boolean isOppositeInterfaceVertical, stemRight;

		public Class<? extends Tile> tileType;

		public Class<? extends Tile> stairType;

		public StairsCreationCustomization(boolean stemUp, boolean stemRight, boolean isOppositeInterfaceVertical, int length, int reciprocalGradient, int corridorHeight, int borderThickness, Class<? extends Tile> tileType, Class<? extends Tile> stairType) {
			this.stemRight = stemRight;
			this.isOppositeInterfaceVertical = isOppositeInterfaceVertical;
			this.length = length;
			this.reciprocalGradient = reciprocalGradient;
			this.corridorHeight = corridorHeight;
			this.borderThickness = borderThickness;
			this.tileType = tileType;
			this.stairType = stairType;

			if (stemUp) {
				this.slopeGradient = stemRight ? 1f/reciprocalGradient : -1f/reciprocalGradient;
			} else {
				this.slopeGradient = stemRight ? -1f/reciprocalGradient : 1f/reciprocalGradient;
			}
		}
	}
}
