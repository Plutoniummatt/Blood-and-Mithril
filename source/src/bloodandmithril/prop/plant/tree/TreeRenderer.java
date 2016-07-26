package bloodandmithril.prop.plant.tree;

import static java.lang.Math.sin;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Timers;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.Textures;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.renderservice.PropRenderingService;
import bloodandmithril.world.weather.WeatherService;

/**
 * Renders {@link TreeSegment}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class TreeRenderer implements PropRenderingService {

	@Inject private Graphics graphics;
	@Inject private WeatherService weatherService;
	@Inject private Timers timers;
	@Inject private TreeTraversalService treeTraversalService;
	
	@Override
	public void render(Prop p) {
		Tree tree = (Tree) p;
		Class<? extends Tree> treeClass = tree.getClass();
		
		treeTraversalService.topDown(tree, (segment, position, angle, thinningFactor, thinningFactorStep, curvature, overlap, lengthFactor) -> {
			TextureRegion textureRegion = Textures.treeTextures.get(treeClass).get(segment.textureId);
			
			graphics.getSpriteBatch().draw(
				textureRegion,
				position.x, 
				position.y,
				segment.width/2,
				0f,
				textureRegion.getRegionWidth(),
				textureRegion.getRegionHeight() * lengthFactor,
				thinningFactor,
				1f,
				angle
			);
			
			if (segment.getLeaves() != null) {
				TextureRegion leavesTexture = Textures.treeTextures.get(treeClass).get(segment.getLeaves().textureId);
				float wind = weatherService.getWind(tree.getWorldId(), position);
				graphics.getSpriteBatch().draw(
					leavesTexture,
					position.x, 
					position.y,
					segment.width/2,
					0f,
					leavesTexture.getRegionWidth(),
					leavesTexture.getRegionHeight(),
					1f,
					1f,
					angle + 2f * (float) sin(timers.renderUtilityTime * 6f + segment.hashCode() / 100000f) * wind
				);
			}
		});
	}
}