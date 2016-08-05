package bloodandmithril.generation.component.components.stemming;

import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.stemming.interfaces.Interface;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class ComponentBuilder<C extends Component> {
	
	protected Optional<Interface> toStemFrom = Optional.absent();
	protected Optional<Integer> structureKey = Optional.absent();

	/**
	 * @return the built {@link Component}
	 */
	public abstract C build();
	
	
	/**
	 * @param iface to stem from
	 */
	public void setInterfaceToStemFrom(Interface iface) {
		toStemFrom = Optional.of(iface);
	}
	
	
	/**
	 * @param structureKey this {@link ComponentBuilder} will build the {@link Component} on
	 */
	public ComponentBuilder<C> withStructureKey(int structureKey) {
		this.structureKey = Optional.of(structureKey);
		return this;
	}
}