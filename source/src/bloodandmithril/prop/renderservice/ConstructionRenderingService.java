package bloodandmithril.prop.renderservice;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.util.Shaders;

/**
 * Renders {@link Construction}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ConstructionRenderingService implements PropRenderingService {
	
	@Inject private Graphics graphics;

	@Override
	public void render(Prop p) {
		Construction construction = (Construction) p;
		
		if (construction.getConstructionProgress() == 0f) {
			Shaders.filter.setUniformf("color", 1f, 1f, 1f, 0.90f);
		}
		
		graphics.getSpriteBatch().draw(
			construction.getTextureRegion(), 
			construction.position.x - construction.width / 2, 
			construction.position.y
		);
	}
}