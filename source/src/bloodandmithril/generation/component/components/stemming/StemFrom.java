package bloodandmithril.generation.component.components.stemming;

import java.util.Optional;
import java.util.function.Predicate;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structures;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.stemming.interfaces.Interface;
import bloodandmithril.generation.component.components.stemming.interfaces.StemmingDirection;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public class StemFrom implements StemFromInterface {
	
	private final Component componentToStemFrom;
	private int interfaceOffset;
	private Predicate<Class<? extends Interface>> interfaceToStemFrom = iface -> false;
	private Predicate<StemmingDirection> directionFilter = direction -> true;
	
	/**
	 * Constructor
	 */
	public StemFrom(Component componentToStemFrom) {
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
	
	
	/**
	 * Specifies a {@link StemmingDirection} filter for interfaces to be used
	 */
	@Override
	public StemFromInterface withDirection(StemmingDirection direction) {
		this.directionFilter = dir -> dir == direction;
		return this;
	}


	@Override
	public <C extends Component, D extends ComponentBuilder<C>> D using(Class<D> builder) {
		try {
			Optional<Interface> toStemFrom = componentToStemFrom.getInterfaces()
			.stream()
			.filter(iface -> interfaceToStemFrom.test(iface.getClass()) && directionFilter.test(iface.getStemmingDirection()))
			.findAny();
			
			D componentBuilder = builder.newInstance();
			componentBuilder.setParentStructure(Structures.get(componentToStemFrom.getStructureKey()));
			componentBuilder.setInterfaceOffset(interfaceOffset);
			
			if (toStemFrom.isPresent()) {
				componentBuilder.setComponentAndInterfaceToStemFrom(toStemFrom.get(), componentToStemFrom);
			} else {
				componentBuilder.dummy();
			}
			
			return componentBuilder;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public StemFromInterface specifyOffset(int offset) {
		this.interfaceOffset = offset;
		return this;
	}
}