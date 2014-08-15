package bloodandmithril.generation.component.interfaces;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.generation.component.Component;
import bloodandmithril.generation.component.Component.ComponentCreationCustomization;

import com.badlogic.gdx.graphics.Color;

/**
 * An interface is a region of a {@link Component} which is used to connect to other {@link Component}s via their interfaces
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Interface implements Serializable {
	private static final long serialVersionUID = -4317621291194222673L;


	/**
	 * Create an interface that is already connected to this one.
	 */
	public abstract Interface createConnectedInterface(InterfaceCustomization customization);


	/**
	 * Create a component of the specified type from this interface
	 */
	public abstract <T extends Component> Component createComponent(Class<T> type, ComponentCreationCustomization<T> custom, int structureKey);


	/**
	 * Renders this {@link Interface}, for debugging purposes
	 */
	public abstract void render(Color color);


	/**
	 * A customization class for each implementation of {@link Interface}
	 *
	 * @author Matt
	 */
	public static abstract class InterfaceCustomization {
	}
}
