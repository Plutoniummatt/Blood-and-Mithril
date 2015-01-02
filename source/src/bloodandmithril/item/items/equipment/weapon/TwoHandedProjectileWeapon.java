package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.math.Vector2;

public abstract class TwoHandedProjectileWeapon<T extends Material> extends TwoHandedMeleeWeapon<T> implements RangedWeapon {
	private static final long serialVersionUID = -2521713935061413566L;
	
	/**
	 * Constructor
	 */
	protected TwoHandedProjectileWeapon(float mass, int volume, boolean equippable, long value, Class<T> material) {
		super(mass, volume, equippable, value, material);
	}
	
	@Override
	public Projectile fire(Vector2 origin, Vector2 direction) {
		return null;
	}
}