package spritestar.character.ai.pathfinding;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

import spritestar.character.Individual;
import spritestar.character.ai.pathfinding.Path.WayPoint;
import spritestar.world.topography.Topography;
import spritestar.world.topography.tile.Tile;
import spritestar.world.topography.tile.Tile.EmptyTile;

/**
 * The PathFinder abstract class which all path finders should extend
 *
 * @author Matt
 */
public abstract class PathFinder implements Serializable {
	private static final long serialVersionUID = 1227463897714443718L;


	/**
	 * Finds the shortest valid path between two points, applies to flying entities.
	 */
	public abstract Path findShortestPathAir(WayPoint start, WayPoint finish);
	
	
	/**
	 * Finds the shortest valid path between two points.
	 * 
	 * @param height - Number of blocks the {@link Individual} spans vertically
	 * @param safeHeight - Number of blocks the {@link Individual} can fall safely
	 * @param forceTolerance - The tolerance distance such that the {@link Individual} will still attempt to move toward the destination even though no valid path was found
	 */
	public abstract Path findShortestPathGround(WayPoint start, WayPoint finish, int height, int safeHeight, float forceTolerance);
	
	
	/** Get the location of the ground above or below the closest empty or platform space */
	public static Vector2 getGroundAboveOrBelowClosestEmptyOrPlatformSpace(Vector2 location, int radius) {
		Vector2 closestEmptyOrPlatformSpace = getClosestEmptyOrPlatformSpace(location, radius);
		
		if (closestEmptyOrPlatformSpace == null) {
			return null;
		} else {
			return getGroundLocation(closestEmptyOrPlatformSpace, radius * 2);
		}
	}
	
	
	/**
	 *  Get the location of the ground either above the location if on non-empty {@link Tile}
	 *  or the location of the ground below if the location {@link Tile#isPassable()}
	 */
	private static Vector2 getGroundLocation(Vector2 location, int height) {
		int currentHeight = 0;
		if (Topography.getTile(location, true) instanceof EmptyTile) {
			while(Topography.getTile(location.x, location.y + Topography.tileSize * currentHeight, true) instanceof EmptyTile && currentHeight >= -height - 1) {
				currentHeight--;
			}
		} else {
			while(!(Topography.getTile(location.x, location.y + Topography.tileSize * currentHeight, true) instanceof EmptyTile) && currentHeight <= height + 1) {
				currentHeight++;
			}
		}
		
		return Math.abs(currentHeight) >= height + 1 ? null : new Vector2(location.x, location.y + (currentHeight + 1) * Topography.tileSize);
	}
	
	
	/** Returns the position of the closest empty or platform tile */
	private static Vector2 getClosestEmptyOrPlatformSpace(Vector2 location, int radius) {
		if (Topography.getTile(location, true).isPassable()) {
			return location;
		}
		
		for (int i = 1; i <= radius; i++) {
			Vector2 cyclicTileCheck = cyclicTileCheck(location, i);
			if (cyclicTileCheck != null) {
				return cyclicTileCheck;
			}
		}
		
		return null;
	}

	
	/** Cycles through 8 directions of tiles, starting from the top */
	private static Vector2 cyclicTileCheck(Vector2 location, int distance) {
		int additionalDistance = Topography.tileSize * distance;
		float additionalDistanceDiagonal = Topography.tileSize * distance / 1.414f;
		
		// Top
		if (Topography.getTile(location.x, location.y + additionalDistance, true).isPassable()) {
			return new Vector2(location.x, location.y + additionalDistance);
		}
		
		// Top Right
		if (Topography.getTile(location.x + additionalDistanceDiagonal, location.y + additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x + additionalDistanceDiagonal, location.y + additionalDistanceDiagonal);
		}
		
		// Right
		if (Topography.getTile(location.x + additionalDistance, location.y, true).isPassable()) {
			return new Vector2(location.x + additionalDistance, location.y);
		}
		
		// Bottom Right
		if (Topography.getTile(location.x + additionalDistanceDiagonal, location.y - additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x + additionalDistanceDiagonal, location.y - additionalDistanceDiagonal);
		}
		
		// Bottom
		if (Topography.getTile(location.x, location.y - additionalDistance, true).isPassable()) {
			return new Vector2(location.x, location.y - additionalDistance);
		}
		
		// Bottom Left
		if (Topography.getTile(location.x - additionalDistanceDiagonal, location.y - additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x - additionalDistanceDiagonal, location.y - additionalDistanceDiagonal);
		}
		
		// Left
		if (Topography.getTile(location.x - additionalDistance, location.y, true).isPassable()) {
			return new Vector2(location.x - additionalDistance, location.y);
		}
		
		// Top Left
		if (Topography.getTile(location.x - additionalDistanceDiagonal, location.y + additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x - additionalDistanceDiagonal, location.y + additionalDistanceDiagonal);
		}
		
		return null;
	}
}
