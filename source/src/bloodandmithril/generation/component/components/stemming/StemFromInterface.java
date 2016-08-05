package bloodandmithril.generation.component.components.stemming;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.stemming.interfaces.Interface;
import bloodandmithril.generation.component.components.stemming.interfaces.StemmingDirection;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface StemFromInterface {
	
	/**
	 * @param builder to build the {@link Component} with
	 */
	public <C extends Component, D extends ComponentBuilder<C>> D using(Class<D> builder);
	
	
	/**
	 * Specifies an offset which will be applied to the {@link Component} when being stemmed from the {@link Interface}
	 */
	public StemFromInterface specifyOffset(int offset);
	
	
	/**
	 * Specifies a {@link StemmingDirection} filter for interfaces to be used
	 */
	public StemFromInterface withDirection(StemmingDirection direction);
}