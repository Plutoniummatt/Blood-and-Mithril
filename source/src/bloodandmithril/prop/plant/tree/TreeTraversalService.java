package bloodandmithril.prop.plant.tree;

import static java.lang.Math.min;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Operator;
import bloodandmithril.util.datastructure.WrapperForFour;

/**
 * Provides methods to traverse {@link Tree}s, top-down and bottom-up
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TreeTraversalService {

	
	/**
	 * Traverses a {@link Tree} top-down, applying specified {@link Operator} to each {@link TreeSegment}
	 * 
	 * @param tree
	 * @param operator
	 */
	public void topDown(Tree tree, TreeTraversalOperator operator) {
		topDown(
			tree.getStump(), 
			tree.position.cpy().sub(tree.width/2, 0), 
			tree.baseAngle, 
			1f, 
			tree.maxThinningFactor/tree.getHeight(), 
			tree.curvature,
			tree.getTrunkOverlap(), 
			1f,
			operator
		);
	}
	
	
	private void topDown(
		TreeSegment segment,
		Vector2 position, 
		float angle,
		float thinningFactor, 
		float thinningFactorStep, 
		float curvature, 
		int overlap, 
		float lengthFactor,
		TreeTraversalOperator operator
	) {
		if (segment.trunk != null) {
			topDown(
				segment.trunk, 
				position.cpy().add(new Vector2(0, segment.height - overlap).rotate(angle)),
				angle + curvature, 
				thinningFactor - thinningFactorStep, 
				thinningFactorStep, 
				curvature, 
				overlap, 
				lengthFactor,
				operator
			);
		}
		
		for (WrapperForFour<Float, Float, TreeSegment, Operator<TreeSegment>> branch : segment.branches) {
			topDown(
				branch.c, 
				position.cpy().add(new Vector2(0, min(branch.a * lengthFactor, branch.a) * segment.height - overlap / 4).rotate(angle)), 
				angle + branch.b, 
				thinningFactor * 0.78f, 
				0f, 
				0f, 
				overlap, 
				lengthFactor * 0.9f,
				operator
			);
		};
		
		operator.operate(segment, position, angle, thinningFactor, thinningFactorStep, curvature, overlap, lengthFactor);
	}
	
	
	/**
	 * Traverses a {@link Tree} bottom-up, applying specified {@link Operator} to each {@link TreeSegment}
	 * 
	 * @param tree
	 * @param operator
	 */
	public void bottomUp(Tree tree, TreeTraversalOperator operator) {
		bottomUp(
			tree.getStump(), 
			tree.position.cpy().sub(tree.width/2, 0), 
			tree.baseAngle, 
			1f, 
			tree.maxThinningFactor/tree.getHeight(), 
			tree.curvature,
			tree.getTrunkOverlap(), 
			1f,
			operator
		);
	}
	
	
	private void bottomUp(
		TreeSegment segment,
		Vector2 position, 
		float angle,
		float thinningFactor, 
		float thinningFactorStep, 
		float curvature, 
		int overlap, 
		float lengthFactor,
		TreeTraversalOperator operator
	) {
		operator.operate(segment, position, angle, thinningFactor, thinningFactorStep, curvature, overlap, lengthFactor);
		
		for (WrapperForFour<Float, Float, TreeSegment, Operator<TreeSegment>> branch : segment.branches) {
			bottomUp(
				branch.c, 
				position.cpy().add(new Vector2(0, min(branch.a * lengthFactor, branch.a) * segment.height - overlap / 4).rotate(angle)), 
				angle + branch.b, 
				thinningFactor * 0.78f, 
				0f, 
				0f, 
				overlap, 
				lengthFactor * 0.9f,
				operator
			);
		};
		
		if (segment.trunk != null) {
			bottomUp(
				segment.trunk, 
				position.cpy().add(new Vector2(0, segment.height - overlap).rotate(angle)),
				angle + curvature, 
				thinningFactor - thinningFactorStep, 
				thinningFactorStep, 
				curvature, 
				overlap, 
				lengthFactor,
				operator
			);
		}
	}
}