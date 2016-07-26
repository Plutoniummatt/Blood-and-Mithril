package bloodandmithril.prop.plant.tree.alder;

import static java.lang.Math.abs;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Timers;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.plant.tree.TreeSegmentRenderer;
import bloodandmithril.prop.renderservice.PropRenderingService;

/**
 * Renders {@link AlderTree}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class AlderTreeRenderer implements PropRenderingService {
	
	@Inject private Timers timers;
	@Inject private TreeSegmentRenderer treeSegmentRenderer;

	@Override
	public void render(Prop p) {
		AlderTree tree = (AlderTree) p;
		
		float windStrength = 0.0f;
		
		double windStrengthTerm = 1f + 3 * abs(windStrength);
		float swayMagnitude = 0.1f + windStrength * 1.2f;
		
		tree.curvature = (float) Math.sin(
			tree.position.x / (abs(windStrength * 800f) + 1f) + // How in sync the tree sway is 
			timers.renderUtilityTime * windStrengthTerm // Rate of sway
		) * swayMagnitude - windStrength * 4f;
		
		float angle = tree.baseAngle;
		
		Vector2 renderPosition = tree.position.cpy();				
		renderPosition.x = tree.position.x - tree.getStump().width/2;
		renderPosition.y = tree.position.y;
		
		// Top-down render
		treeSegmentRenderer.render(
			tree.getStump(), 
			renderPosition, 
			angle, 
			1f, 
			tree.maxThinningFactor/tree.getHeight(), 
			tree.curvature, 
			tree.getTrunkOverlap(), 
			AlderTree.class, 
			tree.width,
			1f
		);
	}
}