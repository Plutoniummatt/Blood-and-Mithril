package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;

import com.badlogic.gdx.math.Vector2;

/**
 * Ranged weapon interface
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface RangedWeapon {
	
	public static final float rangeControl = 300f;

	/**
	 * Fires a {@link Projectile} from the specified origin in the given direction
	 */
	public Projectile fire(Vector2 origin, Vector2 direction);

	/**
	 * @return true if specified item can be fired by the {@link RangedWeapon}
	 */
	public boolean canFire(Item item);

	/**
	 * Sets the class of ammo to fire
	 */
	public void setAmmo(Item item);

	/**
	 * @return the ammo this {@link RangedWeapon} is selected to fire
	 */
	public Item getAmmo();
}