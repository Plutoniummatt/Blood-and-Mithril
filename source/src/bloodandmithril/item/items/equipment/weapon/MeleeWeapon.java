package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;
import bloodandmithril.util.datastructure.Box;

import com.badlogic.gdx.audio.Sound;

/**
 * A {@link Weapon} used in close quarters combat
 *
 * @author Matt
 */
public abstract class MeleeWeapon<T extends Material> extends Weapon<T> {
	private static final long serialVersionUID = 3679766493927388687L;

	/**
	 * Protected constructor
	 */
	protected MeleeWeapon(float mass, boolean equippable, long value, EquipmentSlot slot, Class<T> material) {
		super(mass, equippable, value, slot, material);
	}


	/**
	 * @return The {@link Box} that will be used to calculate overlaps with other hitboxes
	 */
	public abstract Box getActionFrameHitBox(Individual individual);

	public abstract Sound getHitSound();

	public abstract Sound getBlockSound();

	public abstract float getDisarmChance();

	public abstract float getParryChance();

	public abstract float getParryChanceIgnored();
}
