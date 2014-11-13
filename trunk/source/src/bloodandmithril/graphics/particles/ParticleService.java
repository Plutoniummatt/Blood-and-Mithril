package bloodandmithril.graphics.particles;

import static bloodandmithril.networking.ClientServerInterface.isClient;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableColor;
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
		if (isClient()) {
			for (int i = 0; i < 35; i++) {
				Domain.getActiveWorld().getParticles().add(new TracerParticle(
					position.cpy().add(0, 50f),
					new Vector2(Util.getRandom().nextFloat() * 50f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).mul(5f),
					Color.RED,
					2f,
					Domain.getActiveWorld().getWorldId(),
					new Countdown(Util.getRandom().nextInt(2500)),
					0f,
					MovementMode.GRAVITY
				));
			}
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new BloodSplat(position.cpy(), knockBack));
		}
	}


	public static void flameEmber(Vector2 position, Color color, float glow) {
		if (isClient()) {
			Domain.getActiveWorld().getParticles().add(new TracerParticle(
				position.cpy().add(new Vector2(Util.getRandom().nextFloat() * 10f, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
				new Vector2(Util.getRandom().nextFloat() * 30f, 0f).rotate(Util.getRandom().nextFloat() * 360f),
				color,
				1f,
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(1000)),
				glow,
				MovementMode.EMBER
			));
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new FlameEmber(position.cpy(), new SerializableColor(color), glow));
		}
	}


	public static void parrySpark(Vector2 position, Vector2 knockBack) {

		if (isClient()) {
			for (int i = 0; i < 35; i++) {
				Domain.getActiveWorld().getParticles().add(new TracerParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).mul(2f),
					Color.WHITE,
					1f,
					Domain.getActiveWorld().getWorldId(),
					new Countdown(Util.getRandom().nextInt(100)),
					5f,
					MovementMode.GRAVITY
				));
			}
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new ParrySpark(position.cpy(), knockBack));
		}
	}


	public static class ParrySpark implements Runnable, Serializable {
		private static final long serialVersionUID = -7518924240885920128L;
		private Vector2 position;
		private Vector2 knockBack;

		public ParrySpark(Vector2 position, Vector2 knockBack) {
			this.position = position;
			this.knockBack = knockBack;
		}

		@Override
		public void run() {
			ParticleService.parrySpark(position, knockBack);
		}
	}


	public static class BloodSplat implements Runnable, Serializable {
		private static final long serialVersionUID = -7518924240885920128L;
		private Vector2 position;
		private Vector2 knockBack;

		public BloodSplat(Vector2 position, Vector2 knockBack) {
			this.position = position;
			this.knockBack = knockBack;
		}

		@Override
		public void run() {
			ParticleService.bloodSplat(position, knockBack);
		}
	}


	public static class FlameEmber implements Runnable, Serializable {
		private static final long serialVersionUID = -8479201463865952655L;
		private Vector2 position;
		private SerializableColor color;
		private float glow;

		public FlameEmber(Vector2 position, SerializableColor color, float glow) {
			this.position = position;
			this.color = color;
			this.glow = glow;
		}

		@Override
		public void run() {
			ParticleService.flameEmber(position, color.getColor(), glow);
		}
	}
}