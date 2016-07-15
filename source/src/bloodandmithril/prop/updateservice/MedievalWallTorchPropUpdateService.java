package bloodandmithril.prop.updateservice;

import static bloodandmithril.graphics.Graphics.isOnScreen;

import com.badlogic.gdx.math.Vector2;
import com.google.inject.Singleton;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.prop.Prop;
import bloodandmithril.prop.furniture.MedievalWallTorchProp;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Updates {@link MedievalWallTorchProp}s
 * 
 * @author Matt
 */
@Singleton
@Copyright("Matthew Peck 2016")
public class MedievalWallTorchPropUpdateService implements PropUpdateService {

	@Override
	public void update(Prop prop, float delta) {
		MedievalWallTorchProp torch = (MedievalWallTorchProp) prop;
		
		if (torch.isLit()) {

			if (isOnScreen(torch.position, 50f)) {
				final Vector2 firePosition = torch.position.cpy().add(0, 23);
				ParticleService.randomVelocityDiminishing(firePosition, 3f, 15f, Colors.FIRE_START, Colors.FIRE_START, Util.getRandom().nextFloat() * 3f, 14f, MovementMode.EMBER, Util.getRandom().nextInt(800), Depth.MIDDLEGROUND, false, Colors.FIRE_END);
				ParticleService.randomVelocityDiminishing(firePosition, 3f, 10f, Colors.LIGHT_SMOKE, Colors.LIGHT_SMOKE, 8f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(3000), Depth.BACKGROUND, false, null);
			}
			torch.setBurnDurationRemaining(torch.getBurnDurationRemaining() - delta);

			if (torch.getBurnDurationRemaining() <= 0f) {
				torch.setLit(false);
			}
		}
	}
}