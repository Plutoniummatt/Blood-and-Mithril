package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;
import bloodandmithril.util.datastructure.Box;

/**
 * A {@link Weapon} used in close quarters combat
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class MeleeWeapon<T extends Material> extends Weapon<T> {
	private static final long serialVersionUID = 3679766493927388687L;

	/**
	 * Protected constructor
	 */
	protected MeleeWeapon(float mass, int volume, boolean equippable, long value, EquipmentSlot slot, Class<T> material) {
		super(mass, volume, equippable, value, slot, material);
	}


	/**
	 * @return The {@link Box} that will be used to calculate overlaps with other hitboxes
	 */
	public abstract Box getActionFrameHitBox(Individual individual);

	public abstract int getHitSound();

	public abstract int getBlockSound();

	public abstract float getParryChance();

	public abstract float getParryChanceIgnored();
}
