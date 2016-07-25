package bloodandmithril.prop.renderservice;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.StaticallyRenderedProp;
import bloodandmithril.prop.plant.grass.DryGrass;
import bloodandmithril.prop.plant.grass.GreenGrass;

/**
 * Renders {@link DryGrass} or {@link GreenGrass} using static sprites
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class GrassRenderer implements PropRenderingService {

	@Inject private Graphics graphics;

	@Override
	public void render(Prop p) {
		graphics.getSpriteBatch().draw(((StaticallyRenderedProp) p).getTextureRegion(), p.position.x - p.width / 2, p.position.y - 5);
	}
}
