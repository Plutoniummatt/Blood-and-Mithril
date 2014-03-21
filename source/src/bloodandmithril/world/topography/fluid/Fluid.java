package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.Color;

/**
 * Abstract class representing liquids
 *
 * @author Matt
 */
public abstract class Fluid {

	/** Color of this fluid */
	private Color color;
	
	/** Depth of this fluid, can take value from 1 to {@link Topography#TILE_SIZE} */
	private int depth;
	
	/** Coordinate of this liquid */
	private int tileX, tileY;
	
	/**
	 * Protected constructor
	 */
	protected Fluid(int tileX, int tileY, int depth, Color color) {
		this.setTileX(tileX);
		this.setTileY(tileY);
		this.setDepth(depth);
		this.color = color;
	}
	
	
	/**
	 * Render this liquid
	 */
	public void render() {
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.filledRect(
			TILE_SIZE * getTileX(), 
			TILE_SIZE * getTileY(), 
			TILE_SIZE, 
			getDepth()
		);
	}
	
	
	/**
	 * Internal clone
	 */
	protected abstract Fluid internalClone();
	
	
	/**
	 * Returns a copy of this object
	 */
	public Fluid copy() {
		return internalClone();
	}


	public int getTileX() {
		return tileX;
	}


	public void setTileX(int tileX) {
		this.tileX = tileX;
	}


	public int getTileY() {
		return tileY;
	}


	public void setTileY(int tileY) {
		this.tileY = tileY;
	}


	public int getDepth() {
		return depth;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}
}