package spritestar.world.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import spritestar.util.datastructure.Boundaries;
import spritestar.world.generation.substructures.components.Connection;
import spritestar.world.topography.tile.Tile;

/**
 * Contains methods related to {@link Component}s
 *
 * @author Sam
 */
public class ComponentService {


	/**
	 * Finds and returns the {@link Component} that returns a non-null {@link Tile} at the specified coordinates
	 * 
	 * @param components - the list of {@link Component}s to search
	 */
	public static Tile getTileFromComponentPile(List<Component> components, int worldTileX, int worldTileY, boolean foreground) {
		for (int i = components.size() - 1; i >= 0; i--) {
			Component component = components.get(i);
			if (foreground) {
				if (component.getForegroundTile(worldTileX, worldTileY) != null) {
					return component.getForegroundTile(worldTileX, worldTileY);
				}
			} else {
				if (component.getBackgroundTile(worldTileX, worldTileY) != null) {
					return component.getBackgroundTile(worldTileX, worldTileY);
				}
			}
		}
		return null;
	}
	
	
	/**
	 * Gets the {@link Connection}s from a TreeMap of {@link Component}s
	 * 
	 * @param components - the components you want the connections from.
	 * @return - the TreeMap of Connections.
	 */
	public static TreeMap<Integer, Connection> getConnections(TreeMap<Integer, Component> components) {
		TreeMap<Integer, Connection> connections = new TreeMap<Integer, Connection>();
		for (Map.Entry<Integer, Component> component : components.entrySet()) {
			if (component instanceof Connection) {
				connections.put(component.getKey(), (Connection) component.getValue());
			}
		}
		return connections;
	}
	
	
	/**
	 * Gets the {@link Connection}s from a List of {@link Component}s.
	 * 
	 * @param components - the components you want the connections from.
	 * @return - the List of Connections.
	 */
	public static List<Connection> getConnections(List<Component> components) {
		List<Connection> connections = new ArrayList<Connection>();
		for (Component connection : components) {
			if (connection instanceof Connection) {
				connections.add((Connection) connection);
			}
		}
		return connections;
	}
	
	
	/**
	 * Checks if the boundaries overlap with any {@link Component}s currently in the list.
	 * 
	 * @param components - the list of components to check against.
	 * @param boundaries - the boundaries you're checking space for.
	 */
	public static boolean doBoundariesOverlap(List<Component> components, Boundaries boundaries) {
		for (Component component : components) {
			if (boundaries.top > component.boundaries.bottom && boundaries.top < component.boundaries.top) {
				return false;
			}
			if (boundaries.bottom > component.boundaries.bottom && boundaries.bottom < component.boundaries.top) {
				return false;
			}
			if (boundaries.left > component.boundaries.left && boundaries.left < component.boundaries.right) {
				return false;
			}
			if (boundaries.right > component.boundaries.left && boundaries.right < component.boundaries.right) {
				return false;
			}
		}
		return true;
	}
}