package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_SPEAR;
import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_TWO_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_SPEAR;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_TWO_HANDED_WEAPON;

import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

/**
 * A two-handed {@link Equipable} {@link Weapon}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class TwoHandedMeleeWeapon<T extends Material> extends MeleeWeapon<T> {
	private static final long serialVersionUID = 4831468514592741285L;

	/**
	 * Protected constructor
	 */
	protected TwoHandedMeleeWeapon(final float mass, final int volume, final boolean equippable, final long value, final Class<T> material) {
		super(mass, volume, equippable, value, EquipmentSlot.MAINHAND, material);
	}


	@Override
	public Action getAttackAction(final boolean right) {
		if (right) {
			return stab() ? ATTACK_RIGHT_TWO_HANDED_WEAPON : ATTACK_RIGHT_SPEAR;
		} else {
			return stab() ? ATTACK_LEFT_TWO_HANDED_WEAPON : ATTACK_LEFT_SPEAR;
		}
	}


	@Override
	public int getRenderingIndex(final Individual individual) {
		if (individual instanceof Humanoid) {
			return 5;
		}

		return -1;
	}


	/** Whether to stab or slash */
	public abstract boolean stab();

	@Override
	public boolean twoHand() {
		return true;
	}
}