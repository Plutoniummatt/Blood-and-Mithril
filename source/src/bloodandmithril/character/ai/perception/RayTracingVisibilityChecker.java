package bloodandmithril.character.ai.perception;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;
import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.math.Vector2;

/**
 * Uses a pseudo ray trace algorithm to find a tile type
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class RayTracingVisibilityChecker {


	/**
	 * Ray traces in the specified direction by the specified distance from the specified position to calculate visibility
	 *
	 * @return True if the entire line distance is visible from the specified position in the specified direction
	 */
	public static boolean check(Topography topography, Vector2 position, Vector2 direction, float distance) {
		if (distance < 1f) {
			return true;
		}

		try {
			Tile tile = topography.getTile(position, true);
			TwoInts tileCoords = new TwoInts(
				convertToWorldTileCoord(position.x),
				convertToWorldTileCoord(position.y)
			);

			if (!tile.isTransparent()) {
				return false;
			}

			int x = 0,  y = 0;
			if (direction.x > 0f) {
				x = 1;
			} else {
				x = -1;
			}
			if (direction.y > 0f) {
				y = 1;
			} else {
				y = -1;
			}

			Tile tile1 = topography.getTile(tileCoords.a + x, tileCoords.b, true);
			Tile tile2 = topography.getTile(tileCoords.a, tileCoords.b + y, true);

			if (!tile1.isTransparent() && !tile2.isTransparent()) {
				return false;
			}

			float incrementingDistance = distance - TILE_SIZE / 2 <= 0 ? distance : TILE_SIZE / 2;
			return check(topography, position.cpy().add(direction.cpy().nor().scl(incrementingDistance)), direction, distance - incrementingDistance);
		} catch (NoTileFoundException e) {
			return false;
		}
	}
}