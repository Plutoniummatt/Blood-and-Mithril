package bloodandmithril.prop;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;

/**
 * Serice to update {@link Prop}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class PropUpdater {

	/**
	 * Updates the prop using the correct service
	 */
	public void update(Prop prop, float delta) {
		Wiring.injector().getInstance(prop.getClass().getAnnotation(UpdatedBy.class).value()).update(prop, delta);
	}
}