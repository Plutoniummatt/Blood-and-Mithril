package bloodandmithril.item.items.equipment.weapon;

import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_LEFT_ONE_HANDED_WEAPON_STAB;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON;
import static bloodandmithril.character.individuals.Individual.Action.ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB;
import bloodandmithril.character.individuals.Humanoid;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.Individual.Action;
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
	protected OneHandedMeleeWeapon(float mass, int volume, boolean equippable, long value, Class<T> material) {
		super(mass, volume, equippable, value, EquipmentSlot.MAINHAND, material);
	}


	@Override
	public Action getAttackAction(boolean right) {
		if (right) {
			return stab() ? ATTACK_RIGHT_ONE_HANDED_WEAPON_STAB : ATTACK_RIGHT_ONE_HANDED_WEAPON;
		} else {
			return stab() ? ATTACK_LEFT_ONE_HANDED_WEAPON_STAB : ATTACK_LEFT_ONE_HANDED_WEAPON;
		}
	}


	@Override
	public int getRenderingIndex(Individual individual) {
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
	public void update(Equipper equipper, float delta) {
	}
	
	@Override
	public void onUnequip(Equipper equipper) {
	}
	
	@Override
	public void onEquip(Equipper equipper) {
	}
}