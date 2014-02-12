package bloodandmithril.item.equipment;

import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;

/**
 * A Weapon
 *
 * @author Matt
 */
public abstract class Weapon extends Equipable implements Affector {
	private static final long serialVersionUID = -1099406999441510716L;

	/**
	 * Constructor
	 */
	protected Weapon(float mass, boolean equippable, long value, EquipmentSlot slot) {
		super(mass, equippable, value, slot);
	}
}