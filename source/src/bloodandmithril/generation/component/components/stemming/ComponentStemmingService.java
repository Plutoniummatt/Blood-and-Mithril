package bloodandmithril.generation.component.components.stemming;

import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;

/**
 * Service for stemming {@link Component}s during generation
 *
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class ComponentStemmingService {
	
	/**
	 * @param component to stem from
	 */
	public StemFrom stemFrom(Component component) {
		return new StemFrom(component);
	}
}