package bloodandmithril.core;

import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;

/**
 * An {@link ItemPackage} is essentially a {@link Container} that contains {@link Item}s that can be used to deploy into the game world.
 *
 * @author Matt
 */
public class ItemPackage {

	private final Container container;
	private String name;

	/**
	 * Constructor
	 */
	public ItemPackage(Container container, String name) {
		this.container = container;
		this.name = name;
	}
}