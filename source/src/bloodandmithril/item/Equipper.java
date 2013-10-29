package bloodandmithril.item;

import java.util.HashMap;
import java.util.Map.Entry;

import bloodandmithril.item.equipment.Equipable;

/**
 * An {@link Equipper} is an extention of {@link Container}, that is able to equip {@link Equipable} {@link Item}s
 *
 * @author Matt
 */
public class Equipper extends Container {

	/** The current equipped {@link Item}s of this {@link Container} */
	protected HashMap<Item, Integer> equippedItems = new HashMap<Item, Integer>();

	/** The current available {@link EquipmentSlot}s, maps to true if empty/available */
	protected HashMap<EquipmentSlot, Boolean> availableEquipmentSlots = new HashMap<>();

	/**
	 * @param inventoryMassCapacity
	 */
	protected Equipper(float inventoryMassCapacity) {
		super(inventoryMassCapacity);

		for (EquipmentSlot slot : EquipmentSlot.values()) {
			availableEquipmentSlots.put(slot, true);
		}
	}


	/**
	 * @return the equipped items
	 */
	public HashMap<Item, Integer> getEquipped() {
		return equippedItems;
	}


	/**
	 * Equip an {@link Item}
	 */
	public void equip(Equipable item) {
		for (Item equipped : equippedItems.keySet()) {
			if (equipped.sameAs(item)) {
				return;
			}
		}

		if (availableEquipmentSlots.get(item.slot)) {
			takeItem(item, 1);
			equippedItems.put(item, 1);
			availableEquipmentSlots.put(item.slot, false);
			refreshCurrentLoad();
		} else {
			for (Item eq : equippedItems.keySet()) {
				if (((Equipable)eq).slot.equals(item.slot)) {
					unequip((Equipable)eq);
					break;
				}
			}
			availableEquipmentSlots.put(item.slot, true);
			equip(item);
		}
	}


	/**
	 * Equip an {@link Item}
	 */
	public void unequip(Equipable item) {
		if (equippedItems.containsKey(item)) {
			equippedItems.remove(item);
			inventory.put(item, (inventory.get(item) == null ? 0 : inventory.get(item)) + 1);
			availableEquipmentSlots.put(item.slot, true);
			refreshCurrentLoad();
		}
	}


	/** Refreshes the {@link #currentLoad} */
	private void refreshCurrentLoad() {
		float weight = 0f;
		for (Entry<Item, Integer> entry : inventory.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().mass;
		}
		for (Entry<Item, Integer> entry : equippedItems.entrySet()) {
			weight = weight + entry.getValue() * entry.getKey().mass;
		}
		currentLoad = weight;
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