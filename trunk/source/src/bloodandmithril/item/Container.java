package bloodandmithril.item;

import java.util.Map;

/**
 * A container that contains {@link Item}s
 *
 * @author Matt
 */
public interface Container {

	/**
	 * Synchronizes this container with another
	 */
	public void synchronizeContainer(Container other);


	/**
	 * @param item to put
	 * @param quantity of item to put
	 */
	public void giveItem(Item item);


	/**
	 * Takes a number of items
	 * @return the number of items taken.
	 */
	public int takeItem(Item item);


	/**
	 * @return the inventory
	 */
	public Map<Item, Integer> getInventory();


	/**
	 * @return the maximum weight that can be stored in this {@link ContainerImpl}
	 */
	public float getMaxCapacity();


	/**
	 * @return the current weight that is stored in the {@link ContainerImpl}
	 */
	public float getCurrentLoad();
	
	
	/**
	 * @return whether or not this {@link Container} can exceed the max capacity.
	 */
	public boolean canExceedCapacity();
}