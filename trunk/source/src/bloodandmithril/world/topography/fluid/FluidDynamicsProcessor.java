package bloodandmithril.world.topography.fluid;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.max;
import static java.lang.Math.round;

import java.util.List;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.world.topography.Topography;

/**
 * Class responsible for processing fluid dynamics.
 *
 * @author Matt
 */
public class FluidDynamicsProcessor {

	/** The {@link Topography} instance this processor is responsible for */
	private Topography topography;
	
	/** Whether to reverse sort in the x-direction once y-direction has been sorted */
	private boolean xComparatorReverse;
	
	private static final Vector2 TOP 			= new Vector2( 0f,  1f).nor();
	private static final Vector2 TOPRIGHT 		= new Vector2( 1f,  1f).nor();
	private static final Vector2 RIGHT 			= new Vector2( 1f,  0f).nor();
	private static final Vector2 BOTTOMRIGHT 	= new Vector2( 1f, -1f).nor();
	private static final Vector2 BOTTOM 		= new Vector2( 0f, -1f).nor();
	private static final Vector2 BOTTOMLEFT 	= new Vector2(-1f, -1f).nor();
	private static final Vector2 LEFT 			= new Vector2(-1f,  0f).nor();
	private static final Vector2 TOPLEFT 		= new Vector2(-1f,  1f).nor();
	
	private static List<Vector2> vectors		= newArrayList(TOP, TOPRIGHT, RIGHT, BOTTOMRIGHT, BOTTOM, BOTTOMLEFT, LEFT, TOPLEFT);

	/**
	 * Constructor
	 */
	public FluidDynamicsProcessor(Topography topography) {
		this.topography = topography;
	}
	
	
	/**
	 * Process all {@link Fluid}s on the {@link #topography}
	 */
	public void process() {
		xComparatorReverse = !xComparatorReverse;
		topography.getFluids().getAllFluids().stream()
		.sorted((e1, e2) -> {
			return e1.y.compareTo(e2.y) == 0 ? (xComparatorReverse ? e2.x.compareTo(e1.x) : e1.x.compareTo(e2.x)) : e1.y.compareTo(e2.y);
		})
		.forEach(entry -> {
			processSingleFluid(entry.x, entry.y, entry.value);
		});
	}


	/**
	 * Processes a single fluid
	 */
	private void processSingleFluid(int x, int y, Fluid fluid) {
		hydrostaticPressureCalculation(x, y, fluid);
		hydrodynamicPressureCalculation(x, y, fluid);
		
		flow(x, y, fluid);
	}


	/**
	 * Hydrodynamic pressure calculation method, takes into account neighbouring fluids. (left and right only)
	 */
	private void hydrodynamicPressureCalculation(int x, int y, Fluid fluid) {
		Fluid left = getFluid(x - 1, y);
		Fluid right = getFluid(x + 1, y);
		
		if (left == null || right == null) {
			return;
		} else {
			float total = max(left.getPressure(), right.getPressure());
			fluid.setPressure(fluid.getPressure() + total);
		}
	}


	/**
	 * How do I....flow?
	 */
	private void flow(int x, int y, Fluid fluid) {
		Vector2 netForce = calculateNetForceOn(x, y, fluid);
		fluid.force = netForce; // TODO remove
		
		if (netForce.len() == 0f) {
			return;
		}

		Vector2 directionToFlow = vectors.stream().max((vec1, vec2) -> {
			float forceDotVec1 = netForce.dot(vec1);
			float forceDotVec2 = netForce.dot(vec2);
			
			if (forceDotVec1 > forceDotVec2) {
				return 1;
			} else if (forceDotVec1 < forceDotVec2) {
				return -1;
			} else {
				return 0;
			}
		}).get().cpy();

		float amountToFlow = directionToFlow.dot(netForce);
		
		directionToFlow.x = getNormalisedFloat(directionToFlow.x);
		directionToFlow.y = getNormalisedFloat(directionToFlow.y);
		
		Fluid toFlowInto = getFluid(
			x + round(directionToFlow.x), 
			y + round(directionToFlow.y)
		);
		
		if (toFlowInto == null) {
			if (!isFlowable(round(x + directionToFlow.x), round(y + directionToFlow.y))) {
				return;
			}
			
			topography.getFluids().put(
				x + round(directionToFlow.x), 
				y + round(directionToFlow.y), 
				fluid.sub(amountToFlow)
			);
		} else {
			float max = TILE_SIZE - toFlowInto.getDepth();
			if (max > amountToFlow) {
				toFlowInto.add(fluid.sub(amountToFlow));
			} else {
				toFlowInto.add(fluid.sub(max));
			}
		}
		
		if (fluid.getDepth() == 0f) {
			topography.getFluids().remove(x, y);
		}
	}
	
	
	/**
	 * Returns either -1, 0 or 1, corresponding to f < 0, f == 0 and f > 0.
	 */
	private float getNormalisedFloat(float f) {
		if (f == 0f) {
			return 0f;
		} else if (f < 0f) {
			return -1f;
		} else {
			return 1f;
		}
	}
	

	/**
	 * @return the resultant vector force being exerted on a fluid
	 */
	private Vector2 calculateNetForceOn(int x, int y, Fluid fluid) {
		return new Vector2(
			calculateForceFrom(x - 1, y, Direction.LEFT) - calculateForceFrom(x + 1, y, Direction.RIGHT),
			calculateForceFrom(x, y - 1, Direction.BOTTOM) - calculateForceFrom(x, y + 1, Direction.TOP) - fluid.getDepth() // Last term for gravity
		);
	}
	
	
	/**
	 * @return the forced exerted from this tile.
	 */
	private float calculateForceFrom(int x, int y, Direction direction) {
		if (isFlowable(x, y)) {
			Fluid adjacent = getFluid(x, y);
			if (adjacent == null) {
				return 0f;
			} else {
				return adjacent.getPressure();
			}
		} else {
			switch (direction) {
				case TOP:		return isFlowable(x, y - 2) ? calculateForceFrom(x, y - 2, Direction.BOTTOM) : 0f ;
				case BOTTOM:	return isFlowable(x, y + 2) ? calculateForceFrom(x, y + 2, Direction.TOP) : 0f ;
				case LEFT:		return isFlowable(x + 2, y) ? calculateForceFrom(x + 2, y, Direction.RIGHT) : 0f ;
				case RIGHT:		return isFlowable(x - 2, y) ? calculateForceFrom(x - 2, y, Direction.LEFT) : 0f ;
				default: throw new RuntimeException("Unrecognised direction");
			}
		}
	}

	

	/**
	 * Calculates pressure of a {@link Fluid} considering the stack of {@link Fluid}s above it, purely based on hydrostatic pressure created by gravity
	 */
	private void hydrostaticPressureCalculation(int x, int y, Fluid fluid) {
		if (getFluid(x, y + 1) == null) {
			fluid.setPressure(0f);
			Fluid bottom = getFluid(x, y - 1);
			if (bottom != null) {
				if (bottom.getDepth() == TILE_SIZE) {
					bottom.setPressure(fluid.getDepth() + fluid.getPressure());
					hydrostaticPressureCalculation(x, y - 1, bottom);
				} else {
					bottom.setPressure(0);
				}
			}
		} else {
			Fluid bottom = getFluid(x, y - 1);
			if (bottom != null) {
				bottom.setPressure(fluid.getPressure() + bottom.getDepth());
				hydrostaticPressureCalculation(x, y - 1, bottom);
			}
		}
	}


	/**
	 * @return true if tile at specified location is passable, indicating that fluid can flow through it.
	 */
	private boolean isFlowable(int x, int y) {
		try {
			return topography.getTile(x, y, true).isPassable();
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	
	/**
	 * @return the {@link Fluid} at the specified coordinates, if any.  Null otherwise.
	 */
	private Fluid getFluid(int x, int y) {
		return topography.getFluids().get(x, y);
	}
	
	
	private enum Direction {
		TOP, BOTTOM, LEFT, RIGHT
	}
}