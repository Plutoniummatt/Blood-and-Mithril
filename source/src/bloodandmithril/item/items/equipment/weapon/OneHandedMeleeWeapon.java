package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;

import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

/**
 * A one-handed {@link Equipable} {@link Weapon}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class OneHandedMeleeWeapon<T extends Material> extends MeleeWeapon<T> {
	private static final long serialVersionUID = 2320214226123580597L;

	/**
	 * Protected constructor
	 */
	protected OneHandedMeleeWeapon(final float mass, final int volume, final boolean equippable, final long value, final Class<T> material) {
		super(mass, volume, equippable, value, EquipmentSlot.MAINHAND, material);
		bounces();
	}


	@Override
	public Action getAttackAction(final boolean right) {
		if (right) {
			return stab() ? ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB : ATTACK_RIGHT_ONE_HANDED_WEAPON;
		} else {
			return stab() ? ATTACK_LEFT_ONE_HANDED_WEAPON_STAB : ATTACK_LEFT_ONE_HANDED_WEAPON;
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
		return false;
	}

	@Override
	public void update(final Equipper equipper, final float delta) {
	}

	@Override
	public void onUnequip(final Equipper equipper) {
	}

	@Override
	public void onEquip(final Equipper equipper) {
	}
}