package bloodandmithril.core;

import java.io.Serializable;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;

/**
 * An {@link ItemPackage} is essentially a {@link Container} that contains {@link Item}s that can be used to deploy into the game world.
 *
 * @author Matt
 */
public class ItemPackage implements Serializable {
	private static final long serialVersionUID = -801321038681883210L;
	
	private final Container container;
	private String name;

	/**
	 * Constructor
	 */
	public ItemPackage(Container container, String name) {
		this.container = container;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Container getContainer() {
		return container;
	}
}