package bloodandmithril.world.fluids;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.persistence.ParameterPersistenceService;

/**
 * Represents a fluid strip
 * 
 * @author Sam
 */
@Copyright("Matthew Peck 2016")
public class FluidStrip implements Serializable {
	private static final long serialVersionUID = -5677979058186447121L;

	/** Unique ID of the strip */
	public final int id;
	
	/** World tile coordinates, inclusive */
	public final int worldTileX, worldTileY;
	
	/** Width of the strip, in tiles */
	public final int width;
	
	/** Unique world ID */
	public final int worldId;

	/** Volume of this strip, measured in tiles */
	private float volume;
	
	/** This is incremented by a pressure factor when we try to expel a particle, a particle is expelled every time this reaches 1, then the value is reset to 0 */
	public float pressureCounter = 0f;
	
	/**
	 * Constructor
	 */
	public FluidStrip(int tileX, int tileY, int width, float volume, int worldId) {
		this.id = Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextFluidStripId();
		this.worldTileX = tileX;
		this.worldTileY = tileY;
		this.width = width;
		this.volume = volume;
		this.worldId = worldId;
	}
	
	
	/**
	 * See {@link #volume}
	 */
	public float getVolume() {
		return volume;
	}
	
	
	/**
	 * @param worldTileX
	 * @param worldTileY
	 * @return true if this strip occupies the given world tile coordinates
	 */
	public boolean occupies(int worldTileX, int worldTileY) {
		return 
		this.worldTileY == worldTileY && 				// Same y-level is required
		this.worldTileX <= worldTileX && 				// If the coordinate is to the right of the left edge
		this.worldTileX + this.width > worldTileX;		// If the coordinate is to the left of the right edge
	}
	
	
	/**
	 * @param toAdd
	 * @return the volume that was added
	 */
	public float addVolume(float toAdd) {
		if (toAdd < 0f) {
			if (this.volume > 0f) {
				float previousVolume = this.volume;
				if (this.volume + toAdd < 0f) {
					this.volume = 0f;
					return -previousVolume;
				} else {
					this.volume += toAdd;
					return toAdd;
				}
			} else {
				// Volume can't be negative, should only happen if this.volume == 0f
				return 0f;
			}
		} else {
			this.volume += toAdd;
			return toAdd;
		}
	}
}