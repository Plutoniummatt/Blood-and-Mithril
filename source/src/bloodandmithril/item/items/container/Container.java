package bloodandmithril.item.items.container;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.util.SerializableMappingFunction;

/**
 * A container that contains {@link Item}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public interface Container extends Serializable {


	/**
	 * @return whether the container contains anything at all
	 */
	public boolean isEmpty();


	/**
	 * @return The implementation of the {@link Container} that holds state
	 */
	public Container getContainerImpl();


	/**
	 * Synchronizes this container with another
	 */
	public default void synchronizeContainer(final Container other) {
		getContainerImpl().synchronizeContainer(other);
	}


	/**
	 * @param item to put
	 * @param quantity of item to put
	 */
	public default void giveItem(final Item item) {
		getContainerImpl().giveItem(item);
	}


	/**
	 * @param item to put
	 * @param quantity of item to put
	 */
	public default void giveItem(final Item item, final int quantity) {
		for (int i = quantity; i != 0; i--) {
			getContainerImpl().giveItem(item);
		}
	}


	/**
	 * Takes a number of items
	 * @return the number of items taken.
	 */
	public default int takeItem(final Item item) {
		return getContainerImpl().takeItem(item);
	}


	/**
	 * @return the number of specified items that exists in the container
	 */
	public default int has(final Item item) {
		return getContainerImpl().has(item);
	}


	/**
	 * @return the inventory
	 */
	public default Map<Item, Integer> getInventory() {
		return getContainerImpl().getInventory();
	}


	public default Map<Item, Integer> getItemsSatisfyingPredicate(final SerializableMappingFunction<Entry<Item, Integer>, Boolean> predicate) {
		final Map<Item, Integer> toReturn = Maps.newHashMap();

		for (final Entry<Item, Integer> entry : getInventory().entrySet()) {
			if (predicate.apply(entry)) {
				toReturn.put(entry.getKey(), entry.getValue());
			}
		}

		return toReturn;
	}


	/**
	 * @return the maximum weight that can be stored in this {@link ContainerImpl}
	 */
	public default float getMaxCapacity() {
		return getContainerImpl().getMaxCapacity();
	}


	/**
	 * @return true if this container has a max weight limit.
	 */
	public default boolean getWeightLimited() {
		return true;
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
	public default void setCurrentVolume(final int volume) {
		getContainerImpl().setCurrentVolume(volume);
	}


	/**
	 * @return the current weight that is stored in the {@link ContainerImpl}
	 */
	public default void setCurrentLoad(final float currentLoad) {
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
	public default boolean unlock(final Item with) {
		return getContainerImpl().unlock(with);
	}


	/**
	 * Attempt to lock this container
	 */
	public default boolean lock(final Item with) {
		return getContainerImpl().lock(with);
	}


	/**
	 * @return whether this {@link Container} can take another item
	 */
	public default boolean canReceive(final Item item) {
		return getContainerImpl().canReceive(item);
	}


	/**
	 * @return whether this {@link Container} can take a collection of items
	 */
	public default boolean canReceive(final Collection<Item> items) {
		return getContainerImpl().canReceive(items);
	}
}