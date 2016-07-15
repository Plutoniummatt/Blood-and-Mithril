package bloodandmithril.prop.updateservice;

import static bloodandmithril.graphics.Graphics.isOnScreen;

import com.badlogic.gdx.graphics.Color;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Updates {@link Furnace}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class FurnaceUpdateService extends CraftingStationUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		super.update(prop, delta);
		
		Furnace furnace = (Furnace) prop;

		if (furnace.isOccupied()) {
			if (isOnScreen(furnace.position, 50f)) {
				ParticleService.randomVelocityDiminishing(furnace.position.cpy().add(0, furnace.height - 38), 6f, 30f, Color.ORANGE, Color.ORANGE, 2f, 8f, MovementMode.EMBER, Util.getRandom().nextInt(600), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(furnace.position.cpy().add(0, furnace.height - 38), 6f, 30f, Color.ORANGE, Color.ORANGE, 1f, 6f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Color.RED);
				ParticleService.randomVelocityDiminishing(furnace.position.cpy().add(0, furnace.height - 38), 30f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 10f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.BACKGROUND, false, null);
			}
		}
	}
}
