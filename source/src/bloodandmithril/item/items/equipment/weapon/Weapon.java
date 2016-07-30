package bloodandmithril.item.items.equipment.weapon;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.individuals.Action;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;
import bloodandmithril.util.datastructure.WrapperForTwo;

/**
 * A Weapon
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Weapon<T extends Material> extends Equipable {
	private static final long serialVersionUID = -1099406999441510716L;

	private Class<T> material;

	/**
	 * Constructor
	 */
	protected Weapon(final float mass, final int volume, final boolean equippable, final long value, final EquipmentSlot slot, final Class<T> material) {
		super(mass, volume, equippable, value, slot);
		this.material = material;
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return weaponGetSingular(firstCap);
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return weaponGetPlural(firstCap);
	}

	/**
	 * @return the concurrent attack number of this weapon
	 */
	public abstract int getAttackNumber(Individual attacker);

	/**
	 * @return The name of the {@link Weapon}
	 */
	protected abstract String weaponGetSingular(boolean firstCap);

	/**
	 * @return The name of the {@link Weapon}, plural form
	 */
	protected abstract String weaponGetPlural(boolean firstCap);


	/**
	 * @return The base period of an attack with this {@link Weapon}.
	 */
	public abstract float getBaseAttackPeriod();

	/**
	 * @return The base minimum damage of this {@link Weapon}
	 */
	public abstract float getBaseMinDamage();

	/**
	 * @return The base maximum damage of this {@link Weapon}
	 */
	public abstract float getBaseMaxDamage();

	/**
	 * @return The crit multiplier of this {@link Weapon}
	 */
	public abstract float getCritDamageMultiplier();

	/**
	 * @return the {@link Action} that this {@link Weapon} causes.
	 */
	public abstract Action getAttackAction(boolean right);

	/**
	 * @return the knock back strength
	 */
	public abstract float getKnockbackStrength();

	/**
	 * @return the special effects animation when attacking
	 */
	public abstract WrapperForTwo<Animation, Vector2> getAttackAnimationEffects(Individual individual);

	/**
	 * Weapon-specific special effects
	 *
	 * @param individual to apply the effect to
	 */
	public abstract void specialEffect(Individual individual);

	/**
	 * @return the base crit chance
	 */
	public abstract float getBaseCritChance();

	/**
	 * @return The {@link Material} this {@link Weapon} is made from
	 */
	public Class<T> getMaterial() {
		return material;
	}

	@Override
	public float getUprightAngle() {
		return 0f;
	}
}