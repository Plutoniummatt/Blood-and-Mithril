package bloodandmithril.prop.plant.tree;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;

/**
 * Utility class for traversing trees
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface TreeTraversalOperator {

	/**
	 * Operates on a {@link TreeSegment} given all the traversal parameters
	 */
	public void operate(
		TreeSegment segment,
		Vector2 position, 
		float angle,
		float thinningFactor, 
		float thinningFactorStep, 
		float curvature, 
		int overlap, 
		float lengthFactor
	);
}