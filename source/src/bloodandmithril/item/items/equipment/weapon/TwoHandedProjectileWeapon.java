package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.Equipper;
import bloodandmithril.item.material.Material;

@Copyright("Matthew Peck 2016")
public abstract class TwoHandedProjectileWeapon<T extends Material> extends TwoHandedMeleeWeapon<T> implements RangedWeapon {
	private static final long serialVersionUID = -2521713935061413566L;

	/**
	 * Constructor
	 */
	protected TwoHandedProjectileWeapon(final float mass, final int volume, final boolean equippable, final long value, final Class<T> material) {
		super(mass, volume, equippable, value, material);
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