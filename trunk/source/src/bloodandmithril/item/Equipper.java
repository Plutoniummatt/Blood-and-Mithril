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
	 * @return the available {@link EquipmentSlot}s of this {@link Equipper}
	 */
	public Map<EquipmentSlot, Boolean> getAvailableEquipmentSlots();
	

	/**
	 * @return the equipped items
	 */
	public HashMap<Item, Integer> getEquipped();
	

	/**
	 * Equip an {@link Item}
	 */
	public void equip(Equipable item);


	/**
	 * Equip an {@link Item}
	 */
	public void unequip(Equipable item);

	
	/**
	 * Synchronizes this {@link Container} with another
	 */
	public void synchronizeEquipper(Equipper other);
	

	/**
	 * An equipment slot, {@link Item}s must fit into one of these slots when equipped, there can not be more than one {@link Item} per slot
	 *
	 * @author Matt
	 */
	public enum EquipmentSlot {
		LEFTHAND, RIGHTHAND, GLOVE, CHEST, LEGS, FEET, HEAD, AMMO, NECKLACE, TRINKET
	}
}