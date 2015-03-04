package bloodandmithril.generation.component;

import java.io.Serializable;
import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.ChunkGenerator;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.components.DummyComponent;
import bloodandmithril.generation.component.interfaces.Interface;
import bloodandmithril.generation.component.interfaces.RectangularInterface;
import bloodandmithril.util.Function;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.Domain;
import bloodandmithril.world.WorldProps;
import bloodandmithril.world.topography.tile.Tile;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * A Component is a blueprint for entities to be generated by {@link ChunkGenerator}, that live inside {@link Structure}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Component implements Serializable {
	private static final long serialVersionUID = 7335059774362898508L;

	/** Available {@link Interface}s of this {@link Component} */
	private final List<Interface> availableInterfaces = Lists.newArrayList();

	/** {@link Interface}s of existing stemmed {@link Component}s. */
	private final List<Interface> existingInterfaces = Lists.newArrayList();

	/** {@link Boundaries} of this {@link Component} */
	protected final Boundaries boundaries;

	/** The key of the {@link Structure} this {@link Component} exists on */
	private final int structureKey;

	/** Predicate used to filter a collection of interfaces, leaving the vertical interfaces */
	protected static Predicate<Interface> verticalInterfacePredicate = new Predicate<Interface>() {
		@Override
		public boolean apply(Interface input) {
			if (input instanceof RectangularInterface) {
				RectangularInterface iface = (RectangularInterface) input;
				return iface.boundaries.left == iface.boundaries.right && iface.boundaries.top != iface.boundaries.bottom;
			} else {
				throw new RuntimeException();
			}
		}
	};

	/**
	 * Constructor
	 */
	protected Component(Boundaries boundaries, int structureKey) {
		this.boundaries = boundaries;
		this.structureKey = structureKey;
		if (structureKey != -1) {
			addProps(Domain.getWorld(Structures.get(getStructureKey()).worldId).props());
		}
	}


	/**
	 * Gets the foreground tile of this component
	 */
	public abstract Tile getForegroundTile(int worldTileX, int worldTileY);


	/**
	 * Gets the background tile of this component
	 */
	public abstract Tile getBackgroundTile(int worldTileX, int worldTileY);


	/**
	 * Generates all {@link Interface}s
	 */
	protected abstract void generateInterfaces();


	/**
	 * Adds props
	 */
	protected abstract void addProps(WorldProps props);


	/**
	 * Stem a {@link Component} of the specified type from this {@link Component}.
	 */
	protected abstract <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom);


	/**
	 * Attempt to stem from this {@link Component}, another {@link Component}, returning the stemmed component if the attempt was successful, null otherwise
	 */
	public <T extends Component> Component stem(Structure on, Class<T> with, Function<? extends ComponentCreationCustomization<T>> customFunction) {
		return attemptStem(on, with, customFunction, 0);
	}


	/**
	 * Attempt to stem a component on a structure, from this component.
	 *
	 * 10 consecutive failed attempts will result in a {@link DummyComponent} being returned, thus breaking the component stem-chain
	 */
	private <T extends Component> Component attemptStem(Structure on, Class<T> with, Function<? extends ComponentCreationCustomization<T>> customFunction, int attempts) {
		// If we've attempted to stem 10 times without success, give up and break the stem-chain
		if (attempts >= 10) {
			return DummyComponent.getInstance();
		}

		// Clear and regenerate interfaces
		getAvailableInterfaces().clear();
		generateInterfaces();

		// Stem and generate stemmed component
		Component stemmedComponent = internalStem(with, customFunction.call());

		// Clear and regenerate interfaces again, strictly speaking this isn't necessary, it is merely more convenient for development
		getAvailableInterfaces().clear();
		generateInterfaces();

		// If it was not possible to stem a component, then try again, otherwise we can add the stemmed component to the component list on the structure
		if (stemmedComponent == null) {
			return attemptStem(on, with, customFunction, attempts + 1);
		} else {
			// But only add the component if its not a dummy
			if (!(stemmedComponent instanceof DummyComponent)) {
				on.getComponents().add(stemmedComponent);
			}
			return stemmedComponent;
		}
	}


	/**
	 * Attempt to stem from this {@link Component}, another {@link Component}, returning the stemmed component if the attempt was successful, null otherwise
	 */
	public <T extends Component> Component stem(Structure on, Class<T> with, Function<? extends ComponentCreationCustomization<T>> customFunction, int times) {
		Component last = stem(on, with, customFunction);

		for (int i = 1; i < times; i++) {
			last = last.stem(on, with, customFunction);
		}

		return last;
	}


	/**
	 * Checks if the newly created component overlaps with any other component on the structure, excluding *this* component
	 *
	 * If overlap detected, do not add created interface to existing interfaces list.
	 */
	protected Component checkForOverlaps(Interface createdInterface, Component createdComponent) {
		for (Component component : Structures.get(getStructureKey()).getComponents()) {
			if (component == this) {
				continue;
			} else {
				if (component.getBoundaries().overlapsWith(createdComponent.getBoundaries())) {
					Logger.generationDebug("Overlap detected when stemming " + getClass().getSimpleName(), LogLevel.INFO);
					return null;
				}
			}
		}

		// Add created interface to existing interfaces of both components
		getExistingInterfaces().add(createdInterface);
		createdComponent.getExistingInterfaces().add(createdInterface);
		createdComponent.generateInterfaces();

		return createdComponent;
	}


	/**
	 * See {@link #availableInterfaces}
	 */
	public List<Interface> getAvailableInterfaces() {
		return availableInterfaces;
	}


	/**
	 * See {@link #existingInterfaces}
	 */
	public List<Interface> getExistingInterfaces() {
		return existingInterfaces;
	}


	/**
	 * See {@link #boundaries}
	 */
	public Boundaries getBoundaries() {
		return boundaries;
	}


	/**
	 * Gets the unique key of the structure which this {@link Component} exists on
	 */
	public int getStructureKey() {
		return structureKey;
	}


	/**
	 * Generates horizontal {@link RectangularInterface}s, of unit thickness, which takes into account the {@link #existingInterfaces}
	 */
	protected void generateUnitThicknessHorizontalInterfaces(int y, int left, int right) {
		Integer mostRightAvailableX = null;

		for (int x = right; x >= left; x--) {
			boolean overlap = false;

			for (Interface iface : getExistingInterfaces()) {
				if (iface instanceof RectangularInterface) {
					overlap = ((RectangularInterface) iface).boundaries.isWithin(x, y) || overlap;
				}
			}

			if (overlap) {
				if (mostRightAvailableX != null) {
					RectangularInterface rectangularInterface = new RectangularInterface(new Boundaries(y, y, x + (x == right ? 0 : 2), mostRightAvailableX));
					if (!(rectangularInterface.getWidth() <= 1)) {
						getAvailableInterfaces().add(rectangularInterface);
					}
					mostRightAvailableX = null;
				}
			} else {
				if (mostRightAvailableX == null) {
					mostRightAvailableX = x - (x == left || x == right ? 0 : 1);
				} else {
					if (x == left) {
						RectangularInterface rectangularInterface = new RectangularInterface(new Boundaries(y, y, x, mostRightAvailableX));
						if (!(rectangularInterface.getWidth() <= 1)) {
							getAvailableInterfaces().add(rectangularInterface);
						}
					}
				}
			}
		}
	}


	/**
	 * Generates vertical {@link RectangularInterface}s, of unit thickness, which takes into account the {@link #existingInterfaces}
	 */
	protected void generateUnitThicknessVerticalInterfaces(int x, int top, int bottom) {
		Integer highestAvailableY = null;

		for (int y = top; y >= bottom; y--) {
			boolean overlap = false;

			for (Interface iface : getExistingInterfaces()) {
				if (iface instanceof RectangularInterface) {
					overlap = ((RectangularInterface) iface).boundaries.isWithin(x, y) || overlap;
				}
			}

			if (overlap) {
				if (highestAvailableY != null) {
					RectangularInterface rectangularInterface = new RectangularInterface(new Boundaries(highestAvailableY, y + (y == top ? 0 : 2), x, x));
					if (!(rectangularInterface.getHeight() <= 1)) {
						getAvailableInterfaces().add(rectangularInterface);
					}
					highestAvailableY = null;
				}
			} else {
				if (highestAvailableY == null) {
					highestAvailableY = y - (y == bottom || y == top ? 0 : 1);
				} else {
					if (y == bottom) {
						RectangularInterface rectangularInterface = new RectangularInterface(new Boundaries(highestAvailableY, y, x, x));
						if (!(rectangularInterface.getHeight() <= 1)) {
							getAvailableInterfaces().add(rectangularInterface);
						}
					}
				}
			}
		}
	}


	/**
	 * Customization values used when creating a {@link Component} from an {@link Interface}
	 *
	 * @author Matt
	 */
	public static abstract class ComponentCreationCustomization<T extends Component> {
	}
}