package bloodandmithril.generation;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.persistence.world.ChunkProvider;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.PropPlacementService;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * A utility class that places props, used in generation
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class PropPlacer implements Serializable {
	private static final long serialVersionUID = 578683865977423061L;
	private final Prop prop;
	private final Vector2 location;
	private final int worldId;

	/**
	 * Constructor
	 */
	public PropPlacer(Prop prop, Vector2 location, int worldId) {
		this.prop = prop;
		this.location = location;
		this.worldId = worldId;
	}


	/**
	 * Attempts to place the prop, return true if successful
	 */
	boolean place() {
		try {
			Vector2 coords;
			if (prop.grounded) {
				coords = new Vector2(
					location.x,
					Domain.getWorld(worldId).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(location.x, location.y, true).y
				);
			} else {
				coords = location;
			}
			
			if (Wiring.injector().getInstance(PropPlacementService.class).canPlaceAt(prop, coords)) {
				prop.position.x = coords.x;
				prop.position.y = coords.y;
				Domain.getWorld(worldId).props().addProp(prop);
				return true;
			}

			return false;
		} catch (NoTileFoundException e) {
			Wiring.injector().getInstance(ChunkProvider.class).provideSingleChunk(
				e.chunkX, 
				e.chunkY, 
				Domain.getWorld(worldId), 
				true
			);
			
			return false;
		}
	}


	public Vector2 getLocation() {
		return location;
	}
}