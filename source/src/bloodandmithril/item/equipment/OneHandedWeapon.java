package bloodandmithril.item.equipment;

import bloodandmithril.item.Equipable;
import bloodandmithril.item.Equipper.EquipmentSlot;

/**
 * An {@link Equipable} weapon
 *
 * @author Matt
 */
public abstract class OneHandedWeapon extends Equipable implements Affector {
	private static final long serialVersionUID = 2320214226123580597L;

	/**
	 * Protected constructor
	 */
	protected OneHandedWeapon(float mass, boolean equippable, long value) {
		super(mass, equippable, value, EquipmentSlot.RIGHTHAND);
	}
}