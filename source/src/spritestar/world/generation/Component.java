package spritestar.world.generation;

import static spritestar.util.Util.firstNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import spritestar.util.datastructure.Boundaries;
import spritestar.util.datastructure.Line;
import spritestar.world.generation.substructures.components.Connection;
import spritestar.world.topography.tile.Tile;

/**
 * Multiple {@link Component}s can make up a {@link SubStructure}
 *
 * @author Matt
 */
public abstract class Component implements Serializable {
	private static final long serialVersionUID = 8309735391879343086L;

	/** The key used by this {@link Component}s parent {@link Structure} in the {@link StructureMap} */
	protected int structureKey;

	/** Boundaries of this {@link Component} */
	public Boundaries boundaries;

	/** The Sub-{@link Component}s of this {@link Component} */
	protected final ArrayList<Component> components = new ArrayList<Component>();

	/**
	 * Counts which layer this component is up to.
	 * Used for adding things to {@link components}.
	 */
	private int layerCounter;

	/**
	 * Protected constructor
	 */
	protected Component(Boundaries boundaries) {
		this.boundaries = boundaries;
	}


	/** Generates this {@link Component} */
	public void generate(List<Connection> connectionsToGenerateFrom) {
		generateComponent(connectionsToGenerateFrom);
	}

	/** Generate the main portion of the Component */
	protected abstract void generateComponent(List<Connection> connectionsToGenerateFrom);

	
	/**
	 * @param connectionLine - the line the connection is coming from
	 * @return - the floor this connection needs to meet.
	 */
	public abstract Line getFloorLine(Line connectionLine);
	
	
	/** Adds a connection to this component */
	public abstract void addConnection(Connection connection);
	
	
	/**
	 * Checks if a tile is within the boundaries of the component
	 *
	 * @param worldTileX - the tile to check.
	 * @param worldTileY - the tile to check.
	 * @return - true if out of bounds.
	 */
	private boolean isTileOutOfBounds(int worldTileX, int worldTileY) {
		return worldTileX < boundaries.left || worldTileX > boundaries.right || worldTileY < boundaries.bottom || worldTileY > boundaries.top;
	}


	/**
	 * Adds a {@link Component} to {@link #components}
	 */
	public void addComponent(Component component) {
		if (component.boundaries.top <= this.boundaries.top &&
			component.boundaries.bottom >= this.boundaries.bottom &&
			component.boundaries.left >= this.boundaries.left &&
			component.boundaries.right <= this.boundaries.right
			) {
			components.add(layerCounter, component);
			layerCounter++;
		} else {
			throw new RuntimeException("component didn't fit");
		}
	}


	/**
	 * Sets the key on this component and all components stored within it.
	 *
	 * @param key - The key used by this {@link Component}s parent {@link Structure} in the {@link StructureMap}
	 */
	public void setKeys(int key) {
		this.structureKey = key;
		for(Component component : components) {
			component.setKeys(key);
		}
	}


	/** Returns the foreground tile of this {@link Component} */
	public Tile getForegroundTile(int worldTileX, int worldTileY) {
		if (isTileOutOfBounds(worldTileX, worldTileY)) {
			return null;
		} else {
			return firstNonNull(
				ComponentService.getTileFromComponentPile(components, worldTileX, worldTileY, true),
				getForegroundTileFromComponent(worldTileX, worldTileY)
			);
		}
	}


	/** Returns the background tile of this {@link Component} */
	public Tile getBackgroundTile(int worldTileX, int worldTileY) {
		if (isTileOutOfBounds(worldTileX, worldTileY)) {
			return null;
		} else {
			return firstNonNull(
				ComponentService.getTileFromComponentPile(components, worldTileX, worldTileY, false),
				getBackgroundTileFromComponent(worldTileX, worldTileY)
			);
		}
	}


	/**
	 * @param worldTileX - the x coord of the tile you want.
	 * @param worldTileY - the y coord of the tile you want.
	 * @return - The foreground tile
	 */
	protected abstract Tile getForegroundTileFromComponent(int worldTileX, int worldTileY);


	/**
	 * @param worldTileX - the x coord of the tile you want.
	 * @param worldTileY - the y coord of the tile you want.
	 * @return - the background tile
	 */
	protected abstract Tile getBackgroundTileFromComponent(int worldTileX, int worldTileY);
}