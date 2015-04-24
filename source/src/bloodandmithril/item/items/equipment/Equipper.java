package bloodandmithril.item.items.equipment;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.container.Container;
import bloodandmithril.item.items.equipment.misc.Ring;
import bloodandmithril.util.SerializableFunction;

/**
 * An {@link Equipper} is able to equip {@link Equipable} {@link Item}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Equipper extends Container {


	/**
	 * @return The implementation of {@link Equipper} that holds state
	 */
	public Equipper getEquipperImpl();


	/**
	 * @return the available {@link EquipmentSlot}s of this {@link Equipper}
	 */
	public default ConcurrentHashMap<EquipmentSlot, SerializableFunction<Boolean>> getAvailableEquipmentSlots() {
		return getEquipperImpl().getAvailableEquipmentSlots();
	}


	/**
	 * @return the equipped items
	 */
	public default ConcurrentHashMap<Item, Integer> getEquipped() {
		return getEquipperImpl().getEquipped();
	}


	/**
	 * @return the maximum number of rings this equipper can equip
	 */
	public default int getMaxRings() {
		return getEquipperImpl().getMaxRings();
	}


	/**
	 * @return all equipped {@link Ring}s
	 */
	public default List<Ring> getEquippedRings() {
		return getEquipperImpl().getEquippedRings();
	}


	/**
	 * Equip an {@link Item}
	 */
	public default void equip(Equipable item) {
		if (this instanceof Individual) {
			item.setWorldId(((Individual) this).getWorldId());
		}
		
		getEquipperImpl().equip(item);
		item.onEquip(this);
	}


	/**
	 * Equip an {@link Item}
	 */
	public default void unequip(Equipable item) {
		getEquipperImpl().unequip(item);
		item.onUnequip(this);
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
		OFFHAND, MAINHAND, GLOVE, CHEST, LEGS, FEET, HEAD, AMMO, NECKLACE, RING
	}
}