package bloodandmithril.generation.component.components.stemming;

import java.util.Optional;
import java.util.function.Predicate;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.stemming.interfaces.Interface;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class StemFrom implements StemFromInterface {
	
	private final Component componentToStemFrom;
	private Predicate<Class<? extends Interface>> interfaceToStemFrom = iface -> false;
	
	/**
	 * Constructor
	 */
	StemFrom(Component componentToStemFrom) {
		this.componentToStemFrom = componentToStemFrom;
	}
	
	
	/**
	 * Specify the class of {@link Interface} to stem from
	 */
	public StemFromInterface fromInterface(Class<? extends Interface> interfaceToStemFrom) {
		this.interfaceToStemFrom = iface -> iface.equals(interfaceToStemFrom);
		return this;
	}
	
	
	/**
	 * Specify the class of {@link Interface} to stem from
	 */
	public StemFromInterface fromAnyInterface() {
		this.interfaceToStemFrom = iface -> true;
		return this;
	}


	@Override
	public <C extends Component, D extends ComponentBuilder<C>> D using(Class<D> builder) {
		try {
			Optional<Interface> toStemFrom = componentToStemFrom.getFreeInterfaces()
			.stream()
			.filter(iface -> interfaceToStemFrom.test(iface.getClass()))
			.findAny();
			
			D componentBuilder = builder.newInstance();
			
			if (toStemFrom.isPresent()) {
				componentBuilder.setInterfaceToStemFrom(toStemFrom.get());
			}
			
			return componentBuilder;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}