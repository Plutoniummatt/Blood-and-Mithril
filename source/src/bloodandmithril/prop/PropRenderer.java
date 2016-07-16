package bloodandmithril.prop;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;

/**
 * Serice to render {@link Prop}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PropRenderer {

	/**
	 * Updates the prop using the correct service
	 */
	public void render(Prop prop) {
		Wiring.injector().getInstance(prop.getClass().getAnnotation(RenderPropWith.class).value()).render(prop);
	}
}