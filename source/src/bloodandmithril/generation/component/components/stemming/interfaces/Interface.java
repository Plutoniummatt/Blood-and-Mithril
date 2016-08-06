package bloodandmithril.generation.component.components.stemming.interfaces;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;

/**
 * An interface between stemmed {@link Component}s
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface Interface extends Serializable {
	
	/**
	 * @return the {@link StemmingDirection} of this {@link Interface}
	 */
	public StemmingDirection getStemmingDirection();
}