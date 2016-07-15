package bloodandmithril.prop.updateservice;

import static bloodandmithril.graphics.Graphics.isOnScreen;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Updates {@link Campfire}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class CampfireUpdateService extends CraftingStationUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		super.update(prop, delta);
		
		Campfire campfire = (Campfire) prop;
		
		if (campfire.isLit() && isOnScreen(campfire.position, 50f)) {
			final float size1 = Util.getRandom().nextFloat();
			final float size2 = Util.getRandom().nextFloat();

			ParticleService.randomVelocityDiminishing(campfire.position.cpy().add(0, 13f), 7f, 30f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 10f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(5000), Depth.MIDDLEGROUND, false, null);
			ParticleService.randomVelocityDiminishing(campfire.position.cpy().add(0, 13f), 8f, 15f, Color.WHITE, Colors.FIRE_START, size1 * 3.5f, size1 * 16f + 5f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
			ParticleService.randomVelocityDiminishing(campfire.position.cpy().add(0, 13f), 8f, 15f, Color.WHITE, Colors.FIRE_START, size2 * 3.5f, size2 * 16f + 5f, MovementMode.EMBER, Util.getRandom().nextInt(1200), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
		}
	}
}
