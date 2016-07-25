package bloodandmithril.prop.plant.tree;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.util.datastructure.WrapperForThree;

/**
 * Renders {@link TreeSegment}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TreeSegmentRenderer {

	@Inject private Graphics graphics; 
	
	public void render(
		TreeSegment segment,
		Vector2 renderPosition, 
		float angle,
		float thinningFactor, 
		float thinningFactorStep, 
		float curvature, 
		int overlap, 
		Class<? extends Tree> treeClass, 
		int treeWidth
	) {
		if (segment.trunk != null) {
			render(
				segment.trunk, 
				renderPosition.cpy().add(new Vector2(0, segment.height - overlap).rotate(angle)),
				angle + curvature, 
				thinningFactor - thinningFactorStep, 
				thinningFactorStep, 
				curvature, 
				overlap, 
				treeClass,
				treeWidth
			);
		}
		
		for (WrapperForThree<Float, Float, TreeSegment> branch : segment.branches) {
			render(
				branch.c, 
				renderPosition.cpy().add(new Vector2(0, branch.a * segment.width - overlap).rotate(angle)), 
				angle + branch.b, 
				thinningFactor * 0.35f, 
				0f, 
				0f, 
				overlap, 
				treeClass,
				treeWidth
			);
		}
		
		TextureRegion textureRegion = Textures.trunkTextures.get(treeClass).get(segment.textureId);
		graphics.getSpriteBatch().draw(
			textureRegion,
			renderPosition.x, 
			renderPosition.y,
			segment.width/2,
			0f,
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			thinningFactor,
			1f,
			angle
		);
	}
}