package bloodandmithril.character.ai.perception;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static bloodandmithril.world.topography.Topography.convertToWorldTileCoord;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.datastructure.TwoInts;
import bloodandmithril.world.topography.Topography;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

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
	public static final boolean check(final Topography topography, final Vector2 position, final Vector2 direction, float distance) {
		try {

			Vector2 tempPosition = position.cpy();
			float tempDistance = distance;

			while (tempDistance > 1f) {
				Tile tile = topography.getTile(tempPosition, true);
				TwoInts tileCoords = new TwoInts(
					convertToWorldTileCoord(tempPosition.x),
					convertToWorldTileCoord(tempPosition.y)
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

				float incrementingDistance = tempDistance - TILE_SIZE / 2 <= 0 ? tempDistance : TILE_SIZE / 2;

				tempDistance = tempDistance - incrementingDistance;
				tempPosition = tempPosition.cpy().add(direction.cpy().nor().scl(incrementingDistance));
			}

			return true;
		} catch (NoTileFoundException e) {
			return false;
		}
	}
}