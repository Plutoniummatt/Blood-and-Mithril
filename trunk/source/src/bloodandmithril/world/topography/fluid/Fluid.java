package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.max;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Abstract class representing liquids that flow
 *
 * @author Matt
 */
public class Fluid extends LinkedList<FluidFraction> implements Serializable {
	private static final long serialVersionUID = 2940941435333092614L;
	
	private float depth, pressure;
	
	public Vector2 force; // TODO remove

	/**
	 * Protected constructor
	 */
	public Fluid(float depth, FluidFraction... fluids) {
		this.depth = depth;
		this.pressure = depth;
		addAll(newArrayList(fluids));
	}
	
	
	/**
	 * Protected constructor
	 */
	public Fluid(float depth, Collection<FluidFraction> fluids) {
		this.depth = depth;
		this.pressure = depth;
		addAll(fluids);
	}
	
	
	/**
	 * Render this liquid
	 */
	public void render(int x, int y) {
		Color color = new Color();
		
		stream().forEach(fluidFraction -> {
			color.add(fluidFraction.getLiquid().getColor().cpy().mul(fluidFraction.getFraction()));
		});
		
		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.filledRect(
			TILE_SIZE * x, 
			TILE_SIZE * y, 
			TILE_SIZE, 
			max(Topography.TILE_SIZE, getDepth())
		);
	}
	
	
	/**
	 * Returns a copy of this object
	 */
	public Fluid copy(float depth) {
		return new Fluid(depth, this);
	}
	
	
	public Fluid copy() {
		return new Fluid(depth, this);
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
		recalculateFractions(other);
		depth = depth + other.depth;
		return other.copy(other.depth);
	}

	
	public void recalculateFractions(final Fluid added) {
		final float finalDepth = depth + added.depth;

		//	For each FluidFraction in this fluid (FF1) {
		//		For each FluidFraction in the fluid to be added (FF2) {
		//			Filter out all fluid fractions that are not of the same Liquid as FF1
		//			Mix the fraction of matching FF2 back to original FF1
		//		}
		//	}
		stream().forEach(existingFraction -> {
			added.stream().filter(addedFraction -> {
				return addedFraction.getLiquid().getClass().equals(existingFraction.getLiquid().getClass()); 
			}).forEach(fractionToBeAdded -> {
				existingFraction.setFraction(((existingFraction.getFraction() * depth) + (fractionToBeAdded.getFraction() * added.depth)) / finalDepth);
			});
		});
		
		//	For each FluidFraction in fluid to add (FF2) {
		//		For each FluidFraction in this fluid (FF1) {
		//			Filter out all fluid fractions that are non-existent in all of FF2
		//			Add the fraction of non-existent FF2 to the list of FF1
		//		}
		//	}
		added.stream().filter(addedFraction -> {
			return stream().noneMatch(existingFraction -> {
				return addedFraction.getLiquid().getClass().equals(existingFraction.getLiquid().getClass());
			});
		}).forEach(newFraction -> {
			super.add(FluidFraction.fluid(newFraction.getLiquid(), newFraction.getFraction() * added.depth / finalDepth));
		});
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


	public float getPressure() {
		return pressure;
	}


	public void setPressure(float pressure) {
		this.pressure = pressure;
	}
}