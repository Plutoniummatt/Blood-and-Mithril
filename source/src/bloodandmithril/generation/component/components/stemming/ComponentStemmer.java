package bloodandmithril.generation.component.components.stemming;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;

/**
 * Creates {@link Component}s by stemming, during generation
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface ComponentStemmer<B extends ComponentBuilder<C>, C extends Component> {

	/**
	 * Creates a new {@link Component} instance based on this specific {@link ComponentStemmer}
	 */
	public B create();


	/**
	 * Finalises the component construction and returns the constructed component, or null if not possible to construct
	 */
	public C build();
}