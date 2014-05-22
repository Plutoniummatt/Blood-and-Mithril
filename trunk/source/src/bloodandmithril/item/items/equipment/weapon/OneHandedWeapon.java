package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

/**
 * A one-handed {@link Equipable} {@link Weapon}
 *
 * @author Matt
 */
public abstract class OneHandedWeapon<T extends Material> extends Weapon<T> {
	private static final long serialVersionUID = 2320214226123580597L;

	/**
	 * Protected constructor
	 */
	protected OneHandedWeapon(float mass, boolean equippable, long value, Class<T> material) {
		super(mass, equippable, value, EquipmentSlot.RIGHTHAND, material);
	}


	/**
	 * @return the description of the item type
	 */
	@Override
	public String getType() {
		return "One-handed weapon";
	}
}