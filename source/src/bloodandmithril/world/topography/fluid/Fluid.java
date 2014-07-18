package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.liquid.Liquid;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

/**
 * Abstract class representing liquids that flow
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class Fluid extends LinkedList<FluidFraction> implements Serializable {
	private static final long serialVersionUID = 2940941435333092614L;

	@SuppressWarnings("unused")
	private Fluid() {}

	/**
	 * Constructor
	 */
	public Fluid(FluidFraction... fluids) {
		addAll(newArrayList(fluids));
	}


	/**
	 * Constructor
	 */
	public Fluid(Collection<FluidFraction> fluids) {
		addAll(fluids);
	}


	/**
	 * Constructor
	 */
	public Fluid(Map<Class<? extends Liquid>, Float> map) {
		Collection<FluidFraction> fractions = newArrayList();

		try {
			for (Entry<Class<? extends Liquid>, Float> entry : map.entrySet()) {
				fractions.add(FluidFraction.fraction(entry.getKey().newInstance(), entry.getValue()));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		addAll(fractions);
	}


	/**
	 * Render this liquid
	 */
	public synchronized void render(int x, int y) {
		Color color = new Color();

		Gdx.gl.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);

		stream().forEach(fluidFraction -> {
			color.add(fluidFraction.getLiquid().getColor().cpy().mul(fluidFraction.getDepth() / getDepth()));
		});

		Domain.shapeRenderer.setColor(color);
		Domain.shapeRenderer.filledRect(
			TILE_SIZE * x,
			TILE_SIZE * y,
			TILE_SIZE,
			min(TILE_SIZE, getDepth())
		);
	}


	/**
	 * Returns a copy of this object
	 */
	public synchronized Fluid copy(float depth) {
		float fraction = depth / getDepth();

		Collection<FluidFraction> newFracs = newArrayList();
		stream().forEach(toCopy -> {
			newFracs.add(FluidFraction.fraction(toCopy.getLiquid(), toCopy.getDepth() * fraction));
		});

		return new Fluid(newFracs);
	}


	/**
	 * Returns a copy of this object
	 */
	public synchronized Fluid copy() {
		Collection<FluidFraction> newFracs = newArrayList();
		stream().forEach(toCopy -> {
			newFracs.add(FluidFraction.fraction(toCopy.getLiquid(), toCopy.getDepth()));
		});

		return new Fluid(newFracs);
	}


	public synchronized float getDepth() {
		return (float) stream().mapToDouble(fraction -> {
			return fraction.getDepth();
		}).sum();
	}


	/**
	 * Add another fluid to this one, returning how much was added
	 */
	public synchronized void add(Fluid other) {
		other.stream().forEach(fractionToAdd -> {
			if (stream().filter(matching -> {
				return matching.getLiquid().getClass().equals(fractionToAdd.getLiquid().getClass());
			}).count() == 0) {
				super.add(fractionToAdd);
			} else {
				stream().filter(matching -> {
					return matching.getLiquid().getClass().equals(fractionToAdd.getLiquid().getClass());
				}).forEach(toAddTo -> {
					toAddTo.setDepth(toAddTo.getDepth() + fractionToAdd.getDepth());
				});
			}
		});
	}


	/**
	 * Subtract from this fluid, returning how much was subtracted
	 */
	public synchronized Fluid sub(float amount) {
		if (amount >= getDepth()) {
			Fluid toReturn = copy();
			clear();
			return toReturn;
		} else {
			Fluid toReturn = copy(amount);
			float fraction = amount / getDepth();
			stream().forEach(toMultiply -> {
				toMultiply.setDepth(toMultiply.getDepth() - toMultiply.getDepth() * fraction);
			});
			return toReturn;
		}
	}
}