package bloodandmithril.prop;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.PropPlacement;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.persistence.world.ChunkProvider;
import bloodandmithril.util.Function;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.World;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Contains prop placement related logic
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PropPlacementService {
	
	@Inject private ChunkProvider chunkProvider;
	
	/**
	 * Places a prop via a {@link PropPlacement}, return whether or not placement was successful
	 */
	public boolean placeProp(PropPlacement placement) {
		try {
			Vector2 coords;
			if (placement.prop.grounded) {
				coords = new Vector2(
					placement.location.x,
					Domain.getWorld(placement.worldId).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(placement.location.x, placement.location.y, true).y
				);
			} else {
				coords = placement.location;
			}
			
			if (canPlaceAt(placement.prop, coords)) {
				placement.prop.position.x = coords.x;
				placement.prop.position.y = coords.y;
				Domain.getWorld(placement.worldId).props().addProp(placement.prop);
				return true;
			}

			return false;
		} catch (NoTileFoundException e) {
			chunkProvider.provideSingleChunk(
				e.chunkX, 
				e.chunkY, 
				Domain.getWorld(placement.worldId), 
				true
			);
			
			return false;
		}
	}
	
	
	/**
	 * @return whether this prop can be placed at this props location
	 */
	public boolean canPlaceAtCurrentPosition(Prop prop) throws NoTileFoundException {
		return canPlaceAt(prop, prop.position);
	}
	
	
	/**
	 * @return whether this prop can be placed at a given location
	 */
	public boolean canPlaceAt(Prop propToPlace, Vector2 position) throws NoTileFoundException {
		return canPlaceAt(position.x, position.y, propToPlace.width, propToPlace.height, propToPlace.canPlaceOnTopOf(), propToPlace.canPlaceInFrontOf(), propToPlace.grounded, () -> {
			for (Integer propId : Domain.getWorld(propToPlace.getWorldId()).getPositionalIndexChunkMap().getNearbyEntityIds(Prop.class, position.x, position.y)) {
				Prop prop = Domain.getWorld(propToPlace.getWorldId()).props().getProp(propId);
				if (Domain.getWorld(propToPlace.getWorldId()).props().hasProp(propId)) {
					propToPlace.position.x = position.x;
					propToPlace.position.y = position.y;
					if (propToPlace.id == prop.id || prop.depth == Depth.FRONT || propToPlace.depth == Depth.FRONT) {
						continue;
					}
					if (overlapsWith(propToPlace, prop)) {
						return false;
					}
				}
			}

			return true;
		}, Domain.getWorld(propToPlace.getWorldId()));
	}
	
	
	private boolean overlapsWith(Prop prop, Prop other) {
		float left = prop.position.x - prop.width/2;
		float right = prop.position.x + prop.width/2;
		float top = prop.position.y + prop.height;
		float bottom = prop.position.y;

		float otherLeft = other.position.x - other.width/2;
		float otherRight = other.position.x + other.width/2;
		float otherTop = other.position.y + other.height;
		float otherBottom = other.position.y;

		return
			!(left >= otherRight) &&
			!(right <= otherLeft) &&
			!(top <= otherBottom) &&
			!(bottom >= otherTop);
	}
	

	/**
	 * @return whether this prop can be placed at a given location
	 */
	public boolean canPlaceAt(
		float x, 
		float y, 
		float width, 
		float height, 
		SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf, 
		SerializableMappingFunction<Tile, Boolean> canPlaceInFrontOf, 
		boolean grounded, 
		Function<Boolean> customFunction, 
		World world
	) throws NoTileFoundException {
		float xStep = width / TILE_SIZE;
		long xSteps = Math.round(Math.ceil(xStep));
		float xIncrement = width / xSteps;

		float yStep = height / TILE_SIZE;
		long ySteps = Math.round(Math.ceil(yStep));
		float yIncrement = height / ySteps;

		try {
			for (int i = 0; i <= xSteps; i++) {
				Tile tileUnder = world.getTopography().getTile(x - width / 2 + i * xIncrement, y - TILE_SIZE/2, true);
				if (grounded && (tileUnder.isPassable() || canPlaceOnTopOf != null && !canPlaceOnTopOf.apply(tileUnder))) {
					return false;
				}
				
				if (grounded && (tileUnder.edgeIndex == 1 || tileUnder.edgeIndex == 11)) {
					return false;
				}

				for (int j = 1; j <= ySteps; j++) {
					Tile tileOverlapping = world.getTopography().getTile(x - width / 2 + i * xIncrement, y + j * yIncrement - TILE_SIZE/2, true);
					Tile tileUnderlapping = world.getTopography().getTile(x - width / 2 + i * xIncrement, y + j * yIncrement - TILE_SIZE/2, false);
					if (!tileOverlapping.isPassable() || canPlaceInFrontOf != null && !canPlaceInFrontOf.apply(tileUnderlapping)) {
						return false;
					}
				}
			}
		} catch (NullPointerException e) {
			return false;
		}

		return customFunction.call();
	}
}