package bloodandmithril.graphics.renderers;

import com.google.inject.Inject;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.ProjectileRenderer;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile;

/**
 * @author Matt
 */
@Copyright("Matthew Peck 2017")
public class ArrowRenderer implements ProjectileRenderer {
	@Inject private Graphics graphics;

	@Override
	public void internalRender(Projectile p) {
		graphics.getSpriteBatch().draw(
			ArrowProjectile.textureRegion,
			p.getPosition().x - 25,
			p.getPosition().y - 1.5f,
			25,
			1.5f,
			ArrowProjectile.textureRegion.getRegionWidth(),
			ArrowProjectile.textureRegion.getRegionHeight(),
			1f,
			1f,
			p.getVelocity().angle()
		);		
	}
}