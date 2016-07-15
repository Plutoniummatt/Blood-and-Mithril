package bloodandmithril.prop.updateservice;

import static bloodandmithril.graphics.Graphics.isOnScreen;
import static bloodandmithril.networking.ClientServerInterface.isClient;
import static bloodandmithril.networking.ClientServerInterface.isServer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.audio.SoundService;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Updates {@link BlacksmithWorkshop}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class BlacksmithWorkshopUpdateService extends CraftingStationUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		super.update(prop, delta);
		
		BlacksmithWorkshop workshop = (BlacksmithWorkshop) prop;
		
		if (workshop.isOccupied()) {
			if (isClient()) {
				if (isOnScreen(workshop.position, 50f)) {
					ParticleService.randomVelocityDiminishing(workshop.position.cpy().add(35, workshop.height - 2), 10f, 15f, Colors.FIRE_START, Colors.FIRE_START, Util.getRandom().nextFloat() * 1.5f, 2f, MovementMode.EMBER, Util.getRandom().nextInt(1000), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
					ParticleService.randomVelocityDiminishing(workshop.position.cpy().add(35, workshop.height - 2), 7f, 30f, Colors.LIGHT_SMOKE, Color.BLACK, 5f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(4000), Depth.MIDDLEGROUND, false, null);
				}
			}

			if (workshop.getSparkCountdown() <= 0) {
				if (isClient()) {
					ParticleService.parrySpark(workshop.position.cpy().add(-40, workshop.height - 10), new Vector2(-30f, -100f), Depth.MIDDLEGROUND, Color.WHITE, new Color(1f, 0.8f, 0.3f, 1f), 3500, true, 30, 200f);
				}
				if (isServer()) {
					if (Util.getRandom().nextBoolean()) {
						SoundService.play(SoundService.anvil1, workshop.position.cpy(), true, workshop);
					} else {
						SoundService.play(SoundService.anvil1, workshop.position.cpy(), true, workshop);
					}
				}
				workshop.setSparkCountdown(90);
			}

			workshop.setSparkCountdown(workshop.getSparkCountdown() - 1);
		}
	}
}