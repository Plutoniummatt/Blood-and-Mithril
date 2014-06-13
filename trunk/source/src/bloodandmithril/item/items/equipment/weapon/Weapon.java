package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.character.individuals.Individual.Action;
import bloodandmithril.item.items.equipment.Equipable;
import bloodandmithril.item.items.equipment.Equipper.EquipmentSlot;
import bloodandmithril.item.material.Material;

/**
 * A Weapon
 *
 * @author Matt
 */
public abstract class Weapon<T extends Material> extends Equipable implements Affector {
	private static final long serialVersionUID = -1099406999441510716L;

	private Class<T> material;

	/**
	 * Constructor
	 */
	protected Weapon(float mass, boolean equippable, long value, EquipmentSlot slot, Class<T> material) {
		super(mass, equippable, value, slot);
		this.material = material;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(material).getName() + " " + weaponGetSingular(firstCap);
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(material).getName() + " " + weaponGetPlural(firstCap);
	}

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
	 * @return The base damage of this {@link Weapon}
	 */
	public abstract float getBaseDamage();

	/**
	 * @return the {@link Action} that this {@link Weapon} causes.
	 */
	public abstract Action getAttackAction(boolean right);

	/**
	 * @return The {@link Material} this {@link Weapon} is made from
	 */
	public Class<T> getMaterial() {
		return material;
	}
}