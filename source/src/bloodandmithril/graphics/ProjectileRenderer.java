package bloodandmithril.graphics;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;

/**
 * Renders {@link Projectile}s
 * 
 * @author Matt
 */
@Copyright("Matthew Peck 2017")
public interface ProjectileRenderer {
	public static void render(Projectile p) {
		Wiring.injector().getInstance(p.getClass().getAnnotation(RenderProjectileWith.class).value()).internalRender(p);
	}
	
	public void internalRender(Projectile p);
}