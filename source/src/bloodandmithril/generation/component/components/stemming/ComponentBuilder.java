package bloodandmithril.generation.component.components.stemming;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface ComponentBuilder<C extends Component> {

	/**
	 * @return the built {@link Component}
	 */
	public C build();
}