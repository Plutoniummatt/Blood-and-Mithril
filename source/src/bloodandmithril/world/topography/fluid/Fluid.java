package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import java.io.Serializable;

import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.Color;

/**
 * Abstract class representing liquids
 *
 * @author Matt
 */
public abstract class Fluid implements Serializable {
	private static final long serialVersionUID = 2940941435333092614L;

	/** Color of this fluid */
	private Color color;
	
	/** Depth of this fluid, can take value from 1 to {@link Topography#TILE_SIZE} */
	private float depth;
	
	/**
	 * Protected constructor
	 */
	protected Fluid(float depth, Color color) {
		this.setDepth(depth);
		this.color = color;
	}
	
	
	/**
	 * Render this liquid
	 */
	public void render(int x, int y) {
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.filledRect(
			TILE_SIZE * x, 
			TILE_SIZE * y, 
			TILE_SIZE, 
			getDepth()
		);
	}
	
	
	/**
	 * Internal clone
	 */
	protected abstract Fluid internalClone(float depth);
	
	
	/**
	 * Returns a copy of this object
	 */
	public Fluid copy(float depth) {
		return internalClone(depth);
	}


	public float getDepth() {
		return depth;
	}


	public void setDepth(float depth) {
		if (depth < 0) {
			this.depth = 0;
			return;
		}
		this.depth = depth > TILE_SIZE ? TILE_SIZE : depth;
	}
	
	
	/**
	 * Add another fluid to this one, returning how much was added
	 */
	public Fluid add(Fluid other) {
		if (depth + other.depth > 16) {
			float original = depth;
			depth = 16;
			return other.copy(16 - original);
		} else {
			depth = depth + other.depth;
			return other.copy(other.depth);
		}
	}
	
	
	/**
	 * Subtract from this fluid, returning how much was subtracted
	 */
	public Fluid sub(float amount) {
		if (depth - amount < 0) {
			float original = depth;
			depth = 0;
			return copy(original);
		} else {
			depth = depth - amount;
			return copy(amount);
		}
	}
}