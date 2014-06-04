package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import bloodandmithril.character.individuals.Individual.Action;
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


	@Override
	public Action getAttackAction(boolean right) {
		return right ? ATTACK_RIGHT_ONE_HANDED_WEAPON : ATTACK_LEFT_ONE_HANDED_WEAPON;
	}


	/**
	 * @return the description of the item type
	 */
	@Override
	public String getType() {
		return "One-handed weapon";
	}
}