package bloodandmithril.item.items.equipment.weapon;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;

import com.badlogic.gdx.math.Vector2;

/**
 * Ranged weapon interface
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public interface RangedWeapon {
	
	/**
	 * Fires a {@link Projectile} from the specified origin in the given direction
	 */
	public Projectile fire(Vector2 origin, Vector2 direction);
}