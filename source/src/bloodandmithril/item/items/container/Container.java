package bloodandmithril.item.items.container;

import java.util.Map;

import bloodandmithril.item.items.Item;

/**
 * A container that contains {@link Item}s
 *
 * @author Matt
 */
public interface Container {


	/**
	 * @return The implementation of the {@link Container} that holds state
	 */
	public Container getContainerImpl();


	/**
	 * Synchronizes this container with another
	 */
	public default void synchronizeContainer(Container other) {
		getContainerImpl().synchronizeContainer(other);
	}


	/**
	 * @param item to put
	 * @param quantity of item to put
	 */
	public default void giveItem(Item item) {
		getContainerImpl().giveItem(item);
	}


	/**
	 * Takes a number of items
	 * @return the number of items taken.
	 */
	public default int takeItem(Item item) {
		return getContainerImpl().takeItem(item);
	}


	/**
	 * @return the number of specified items that exists in the container
	 */
	public default int has(Item item) {
		return getContainerImpl().has(item);
	}


	/**
	 * @return the inventory
	 */
	public default Map<Item, Integer> getInventory() {
		return getContainerImpl().getInventory();
	}


	/**
	 * @return the maximum weight that can be stored in this {@link ContainerImpl}
	 */
	public default float getMaxCapacity() {
		return getContainerImpl().getMaxCapacity();
	}


	/**
	 * @return the current weight that is stored in the {@link ContainerImpl}
	 */
	public default float getCurrentLoad() {
		return getContainerImpl().getCurrentLoad();
	}


	/**
	 * @return the current weight that is stored in the {@link ContainerImpl}
	 */
	public default int getCurrentVolume() {
		return getContainerImpl().getCurrentVolume();
	}


	/**
	 * @return the current weight that is stored in the {@link ContainerImpl}
	 */
	public default void setCurrentVolume(int volume) {
		getContainerImpl().setCurrentVolume(volume);
	}


	/**
	 * @return the current weight that is stored in the {@link ContainerImpl}
	 */
	public default void setCurrentLoad(float currentLoad) {
		getContainerImpl().setCurrentLoad(currentLoad);
	}


	/**
	 * @return whether or not this {@link Container} can exceed the max capacity.
	 */
	public default int getMaxVolume() {
		return getContainerImpl().getMaxVolume();
	}


	/**
	 * @return whether or not this {@link Container} is locked.
	 */
	public default boolean isLocked() {
		return getContainerImpl().isLocked();
	}


	/**
	 * @return whether or not this {@link Container} is lockable.
	 */
	public default boolean isLockable() {
		return getContainerImpl().isLockable();
	}


	/**
	 * Attempt to unlock this container
	 */
	public default boolean unlock(Item with) {
		return getContainerImpl().unlock(with);
	}


	/**
	 * Attempt to lock this container
	 */
	public default boolean lock(Item with) {
		return getContainerImpl().lock(with);
	}
}