package bloodandmithril.generation.component;

import java.io.Serializable;
import java.util.List;

import bloodandmithril.generation.Structure;
import bloodandmithril.generation.StructureMap;
import bloodandmithril.generation.TerrainGenerator;
import bloodandmithril.util.Function;
import bloodandmithril.util.Logger;
import bloodandmithril.util.Logger.LogLevel;
import bloodandmithril.util.datastructure.Boundaries;
import bloodandmithril.world.topography.tile.Tile;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * A Component is a blueprint for entities to be generated by {@link TerrainGenerator}, that live inside {@link Structure}s
 *
 * @author Matt
 */
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
	 * Stem a {@link Component} of the specified type from this {@link Component}.
	 */
	protected abstract <T extends Component> Component internalStem(Class<T> with, ComponentCreationCustomization<T> custom);


	/**
	 * Attempt to stem from this {@link Component}, another {@link Component}, returning the stemmed component if the attempt was successful, null otherwise
	 */
	public <T extends Component> Component stem(Structure on, Class<T> with, Function<? extends ComponentCreationCustomization<T>> customFunction) {
		// Clear and regenerate interfaces
		getAvailableInterfaces().clear();
		generateInterfaces();

		// Stem and generate stemmed component
		Component stemmedComponent = internalStem(with, customFunction.call());

		// Clear and regenerate interfaces again, strictly speaking this isn't necessary, it is merely more convenient for development
		getAvailableInterfaces().clear();
		generateInterfaces();
		
		if (stemmedComponent == null) {
			return stem(on, with, customFunction);
		} else {
			on.getComponents().add(stemmedComponent);
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
		for (Component component : StructureMap.structures.get(getStructureKey()).getComponents()) {
			if (component == this) {
				continue;
			} else {
				if (component.getBoundaries().doesOverlapWith(createdComponent.getBoundaries())) {
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


	public List<Interface> getAvailableInterfaces() {
		return availableInterfaces;
	}


	public List<Interface> getExistingInterfaces() {
		return existingInterfaces;
	}


	public Boundaries getBoundaries() {
		return boundaries;
	}


	public int getStructureKey() {
		return structureKey;
	}


	/**
	 * Customization values used when creating a {@link Component} from an {@link Interface}
	 *
	 * @author Matt
	 */
	public static abstract class ComponentCreationCustomization<T extends Component> {
	}
}