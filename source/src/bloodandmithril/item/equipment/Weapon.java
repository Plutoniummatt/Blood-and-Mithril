package bloodandmithril.item.equipment;

import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;

/**
 * A Weapon
 *
 * @author Matt
 */
public abstract class Weapon extends Equipable implements Affector {

	/**
	 * Constructor
	 */
	protected Weapon(float mass, boolean equippable, long value, EquipmentSlot slot) {
		super(mass, equippable, value, slot);
	}
}