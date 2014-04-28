package bloodandmithril.item;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Equipper} is able to equip {@link Equipable} {@link Item}s
 *
 * @author Matt
 */
public interface Equipper extends Container {


	/**
	 * @return The implementation of {@link Equipper} that holds state
	 */
	public Equipper getEquipperImpl();


	/**
	 * @return the available {@link EquipmentSlot}s of this {@link Equipper}
	 */
	public default Map<EquipmentSlot, Boolean> getAvailableEquipmentSlots() {
		return getEquipperImpl().getAvailableEquipmentSlots();
	}


	/**
	 * @return the equipped items
	 */
	public default HashMap<Item, Integer> getEquipped() {
		return getEquipperImpl().getEquipped();
	}


	/**
	 * Equip an {@link Item}
	 */
	public default void equip(Equipable item) {
		getEquipperImpl().equip(item);
	}


	/**
	 * Equip an {@link Item}
	 */
	public default void unequip(Equipable item) {
		getEquipperImpl().unequip(item);
	}


	/**
	 * Synchronizes this {@link Container} with another
	 */
	public default void synchronizeEquipper(Equipper other) {
		getEquipperImpl().synchronizeEquipper(other);
	}


	/**
	 * An equipment slot, {@link Item}s must fit into one of these slots when equipped, there can not be more than one {@link Item} per slot
	 *
	 * @author Matt
	 */
	public enum EquipmentSlot {
		LEFTHAND, RIGHTHAND, GLOVE, CHEST, LEGS, FEET, HEAD, AMMO, NECKLACE, TRINKET
	}
}