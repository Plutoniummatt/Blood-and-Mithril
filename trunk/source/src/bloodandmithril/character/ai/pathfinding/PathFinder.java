package bloodandmithril.character.ai.pathfinding;

import java.io.Serializable;

import bloodandmithril.character.ai.pathfinding.Path.WayPoint;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;
import bloodandmithril.world.topography.tile.Tile.EmptyTile;

import com.badlogic.gdx.math.Vector2;


/**
 * The PathFinder abstract class which all path finders should extend
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class PathFinder implements Serializable {
	private static final long serialVersionUID = 1227463897714443718L;

	/**
	 * Finds the shortest valid path between two points, applies to flying entities.
	 */
	public abstract Path findShortestPathAir(WayPoint start, WayPoint finish, World world);


	/**
	 * Finds the shortest valid path between two points.
	 *
	 * @param height - Number of blocks the {@link Individual} spans vertically
	 * @param safeHeight - Number of blocks the {@link Individual} can fall safely
	 * @param forceTolerance - The tolerance distance such that the {@link Individual} will still attempt to move toward the destination even though no valid path was found
	 */
	public abstract Path findShortestPathGround(WayPoint start, WayPoint finish, int height, int safeHeight, float forceTolerance, World world);


	/** Get the location of the ground above or below the closest empty or platform space */
	public static Vector2 getGroundAboveOrBelowClosestEmptyOrPlatformSpace(Vector2 location, int radius, World world) throws NoTileFoundException {
		Vector2 closestEmptyOrPlatformSpace = getClosestEmptyOrPlatformSpace(location, radius, world);

		if (closestEmptyOrPlatformSpace == null) {
			return null;
		} else {
			return getGroundLocation(closestEmptyOrPlatformSpace, radius * 2, world);
		}
	}


	/**
	 *  Get the location of the ground either above the location if on non-empty {@link Tile}
	 *  or the location of the ground below if the location {@link Tile#isPassable()}
	 */
	private static Vector2 getGroundLocation(Vector2 location, int height, World world) throws NoTileFoundException {
		int currentHeight = 0;
		if (world.getTopography().getTile(location, true) instanceof EmptyTile) {
			while(world.getTopography().getTile(location.x, location.y + Topography.TILE_SIZE * currentHeight, true) instanceof EmptyTile && currentHeight >= -height - 1) {
				currentHeight--;
			}
		} else {
			while(!(world.getTopography().getTile(location.x, location.y + Topography.TILE_SIZE * currentHeight, true) instanceof EmptyTile) && currentHeight <= height + 1) {
				currentHeight++;
			}
		}

		return Math.abs(currentHeight) >= height + 1 ? null : new Vector2(location.x, location.y + (currentHeight + 1) * Topography.TILE_SIZE);
	}


	/** Returns the position of the closest empty or platform tile */
	private static Vector2 getClosestEmptyOrPlatformSpace(Vector2 location, int radius, World world) throws NoTileFoundException {
		if (world.getTopography().getTile(location, true).isPassable()) {
			return location;
		}

		for (int i = 1; i <= radius; i++) {
			Vector2 cyclicTileCheck = cyclicTileCheck(location, i, world);
			if (cyclicTileCheck != null) {
				return cyclicTileCheck;
			}
		}

		return null;
	}


	/** Cycles through 8 directions of tiles, starting from the top */
	private static Vector2 cyclicTileCheck(Vector2 location, int distance, World world) throws NoTileFoundException {
		int additionalDistance = Topography.TILE_SIZE * distance;
		float additionalDistanceDiagonal = Topography.TILE_SIZE * distance / 1.414f;

		// Top
		if (world.getTopography().getTile(location.x, location.y + additionalDistance, true).isPassable()) {
			return new Vector2(location.x, location.y + additionalDistance);
		}

		// Top Right
		if (world.getTopography().getTile(location.x + additionalDistanceDiagonal, location.y + additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x + additionalDistanceDiagonal, location.y + additionalDistanceDiagonal);
		}

		// Right
		if (world.getTopography().getTile(location.x + additionalDistance, location.y, true).isPassable()) {
			return new Vector2(location.x + additionalDistance, location.y);
		}

		// Bottom Right
		if (world.getTopography().getTile(location.x + additionalDistanceDiagonal, location.y - additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x + additionalDistanceDiagonal, location.y - additionalDistanceDiagonal);
		}

		// Bottom
		if (world.getTopography().getTile(location.x, location.y - additionalDistance, true).isPassable()) {
			return new Vector2(location.x, location.y - additionalDistance);
		}

		// Bottom Left
		if (world.getTopography().getTile(location.x - additionalDistanceDiagonal, location.y - additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x - additionalDistanceDiagonal, location.y - additionalDistanceDiagonal);
		}

		// Left
		if (world.getTopography().getTile(location.x - additionalDistance, location.y, true).isPassable()) {
			return new Vector2(location.x - additionalDistance, location.y);
		}

		// Top Left
		if (world.getTopography().getTile(location.x - additionalDistanceDiagonal, location.y + additionalDistanceDiagonal, true).isPassable()) {
			return new Vector2(location.x - additionalDistanceDiagonal, location.y + additionalDistanceDiagonal);
		}

		return null;
	}
}
