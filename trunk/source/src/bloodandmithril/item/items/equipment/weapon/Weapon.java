package bloodandmithril.item.items.equipment.weapon;

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
	 * @return The {@link Material} this {@link Weapon} is made from
	 */
	public Class<T> getMaterial() {
		return material;
	}
}