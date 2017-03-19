package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.ProjectileRenderer;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;

/**
 * Dummy version of the {@link ProjectileRenderer}
 * 
 * @author Matt
 */
@Copyright("Matthew Pecj 2017")
public class DummyProjectileRenderer implements ProjectileRenderer {
	@Override
	public void internalRender(Projectile p) {
		throw new RuntimeException();
	}
}