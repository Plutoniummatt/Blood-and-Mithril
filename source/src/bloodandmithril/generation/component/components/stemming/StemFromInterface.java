package bloodandmithril.generation.component.components.stemming;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface StemFromInterface {
	
	/**
	 * @param builder to build the {@link Component} with
	 */
	public <C extends Component, D extends ComponentBuilder<C>> D using(Class<D> builder);
}