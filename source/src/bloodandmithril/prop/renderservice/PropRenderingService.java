package bloodandmithril.prop.renderservice;

import bloodandmithril.core.Copyright;
import bloodandmithril.prop.Prop;

/**
 * Service to render {@link Prop}s
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface PropRenderingService {

	/**
	 * Renders given prop
	 */
	public void render(Prop p);
}