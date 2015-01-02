package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.item.material.Material;

public abstract class TwoHandedProjectileWeapon<T extends Material> extends TwoHandedMeleeWeapon<T> implements RangedWeapon {
	private static final long serialVersionUID = -2521713935061413566L;

	/**
	 * Constructor
	 */
	protected TwoHandedProjectileWeapon(float mass, int volume, boolean equippable, long value, Class<T> material) {
		super(mass, volume, equippable, value, material);
	}
}