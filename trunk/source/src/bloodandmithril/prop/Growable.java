package bloodandmithril.prop;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.topography.tile.Tile;

/**
 * Something can grow
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Growable extends Prop {
	private static final long serialVersionUID = 5474231010517077123L;
	private float growthProgress = 0f;

	/**
	 * Constructor
	 */
	protected Growable(float x, float y, int width, int height, boolean grounded, Depth depth, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		super(x, y, width, height, grounded, depth, canPlaceOnTopOf);
	}

	/**
	 * @return 1.0 if fully grown
	 */
	public float getGrowthProgress() {
		return growthProgress;
	}


	/**
	 * Grows this {@link Growable}
	 */
	public void grow(float amount) {
		if (growthProgress + amount >= 1f) {
			growthProgress = 1f;
		} else {
			growthProgress += amount;
		}

		if (growthProgress <= 0f) {
			growthProgress = 0f;
		}
	}
}