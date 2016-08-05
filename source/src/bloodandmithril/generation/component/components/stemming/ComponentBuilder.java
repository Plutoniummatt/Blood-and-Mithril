package bloodandmithril.generation.component.components.stemming;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.base.Optional;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.Structure;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.components.stemming.interfaces.HorizontalInterface;
import bloodandmithril.generation.component.components.stemming.interfaces.Interface;
import bloodandmithril.generation.component.components.stemming.interfaces.VerticalInterface;
import bloodandmithril.util.Operator;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class ComponentBuilder<C extends Component> {
	
	protected Optional<Structure> parentStructure = Optional.absent();
	protected Optional<Component> componentToStemFrom = Optional.absent();
	protected Optional<Interface> interfaceToStemFrom = Optional.absent();
	protected Optional<Integer> structureKey = Optional.absent();
	protected Operator<Component> stemmingFunction = component -> {};
	
	protected int interfaceOffset;
	protected boolean dummy;
	
	/**
	 * @return the implementation specific build component method, without using {@link Interface}s
	 */
	protected abstract C buildComponent();
	
	
	/**
	 * @param verticalInterface to build the component from
	 */
	protected abstract C buildComponentFrom(VerticalInterface verticalInterface);
	
	
	/**
	 * @param horizontalInterface to build the component from
	 */
	protected abstract C buildComponentFrom(HorizontalInterface horizontalInterface);
	
	
	/**
	 * @return a {@link Component} built from an {@link Interface}
	 */
	protected C buildFromInterface(Interface iface) {
		if (iface instanceof VerticalInterface) {
			return buildComponentFrom((VerticalInterface) iface);
		} else if (iface instanceof HorizontalInterface) {
			return buildComponentFrom((HorizontalInterface) iface);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	

	/**
	 * @return the built {@link Component}
	 */
	public Optional<C> build() {
		if (dummy) {
			return Optional.absent();
		}
		
		C builtComponent = buildComponent();
		builtComponent.generateInterfaces();
		
		MutableBoolean doNotAdd = new MutableBoolean(false);
		if (parentStructure.isPresent()) {
			
			// Do not add the newly created component if it overlaps
			parentStructure.get().getComponents().forEach(component -> {
				if (component != componentToStemFrom.get() && component.getBoundaries().overlapsWith(builtComponent.getBoundaries())) {
					doNotAdd.setTrue();
				}
			});

			if (!doNotAdd.getValue()) {
				parentStructure.get().getComponents().add(builtComponent);
			}
		}
		
		if (!doNotAdd.getValue()) {
			stemmingFunction.operate(builtComponent);
			return Optional.of(builtComponent);
		}
		
		return Optional.absent();
	}
	
	
	/**
	 * @param iface to stem from
	 */
	public void setComponentAndInterfaceToStemFrom(Interface iface, Component component) {
		interfaceToStemFrom = Optional.of(iface);
		componentToStemFrom = Optional.of(component);
	}
	
	
	/**
	 * @param the structure the built component should be added to
	 */
	public void setParentStructure(Structure structure) {
		parentStructure = Optional.of(structure);
	}
	
	
	/**
	 * Indicates that this {@link ComponentBuilder} is a dummy, and thus will produce an absent component
	 */
	public void dummy() {
		this.dummy = true;
	}
	
	
	/**
	 * @param the structure the built component should be added to
	 */
	public void setInterfaceOffset(int offset) {
		this.interfaceOffset = offset;
	}
	
	
	/**
	 * @param stemmingFunction to use to stem from the {@link Component} that this {@link ComponentBuilder} builds
	 */
	public ComponentBuilder<C> withStemmingFunction(Operator<Component> stemmingFunction) {
		this.stemmingFunction = stemmingFunction;
		return this;
	}
	
	
	/**
	 * @param structureKey this {@link ComponentBuilder} will build the {@link Component} on
	 */
	public ComponentBuilder<C> withStructureKey(int structureKey) {
		this.structureKey = Optional.of(structureKey);
		return this;
	}
}