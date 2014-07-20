package bloodandmithril.graphics.particles;

import bloodandmithril.core.Copyright;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

/**
 * Service responsible for adding particles to the world
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ParticleService {

	public static void bloodSplat(Vector2 position, Vector2 knockBack) {
		for (int i = 0; i < 35; i++) {
			Domain.getActiveWorld().getParticles().add(new TracerParticle(
				position.cpy().add(0, 50f),
				new Vector2(Util.getRandom().nextFloat() * 50f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).mul(5f),
				Color.RED,
				2f,
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(2500)),
				0f
			));
		}
	}


	public static void parrySpark(Vector2 position, Vector2 knockBack) {
		for (int i = 0; i < 35; i++) {
			Domain.getActiveWorld().getParticles().add(new TracerParticle(
				position.cpy().add(0, 50f),
				new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).mul(2f),
				Color.WHITE,
				1f,
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(100)),
				5f
			));
		}
	}
}