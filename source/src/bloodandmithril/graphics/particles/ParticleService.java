package bloodandmithril.graphics.particles;

import static bloodandmithril.networking.ClientServerInterface.isClient;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
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
				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 40f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).scl(5f),
					Color.RED,
					Color.RED,
					Color.RED,
					2f,
					Domain.getActiveWorld().getWorldId(),
					0f,
					MovementMode.GRAVITY,
					Depth.FOREGROUND,
					Util.getRandom().nextInt(3000),
					false
				));
			}
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new BloodSplat(position.cpy(), knockBack));
		}
	}


	public static void explosion(Vector2 position) {
		if (isClient()) {
			for (int i = 0; i < 40; i++) {
				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy().add(
						(Util.getRandom().nextFloat() - 0.5f) * 30,
						(Util.getRandom().nextFloat() - 0.5f) * 30
					),
					new Vector2(Util.getRandom().nextFloat() * 30f, 0f).rotate(Util.getRandom().nextFloat() * 360f).scl(5f),
					Colors.LIGHT_SMOKE,
					Colors.LIGHT_SMOKE,
					Colors.LIGHT_SMOKE,
					Util.getRandom().nextFloat() * 45f + 3f,
					Domain.getActiveWorld().getWorldId(),
					0f,
					MovementMode.EMBER,
					Depth.FOREGROUND,
					Util.getRandom().nextInt(5000) + 1000,
					false
				));
			}
			for (int i = 0; i < 100; i++) {
				float radius = Util.getRandom().nextFloat() * 8f;
				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 300f, 0f).rotate(Util.getRandom().nextFloat() * 360f).scl(5f),
					Color.WHITE,
					Color.PINK,
					Color.WHITE,
					radius,
					Domain.getActiveWorld().getWorldId(),
					radius * 5f,
					MovementMode.GRAVITY,
					Depth.FOREGROUND,
					Util.getRandom().nextInt(50),
					true
				));
			}
			for (int i = 0; i < 100; i++) {
				float radius = Util.getRandom().nextFloat() * 2f;
				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).scl(5f),
					Color.WHITE,
					Color.PINK,
					Color.WHITE,
					radius,
					Domain.getActiveWorld().getWorldId(),
					radius * 5f,
					MovementMode.GRAVITY,
					Depth.FOREGROUND,
					Util.getRandom().nextInt(2000),
					true
				));
			}
		}
	}


	public static void fireworks(Vector2 position, Color... color) {
		if (isClient()) {
			for (int i = 0; i < 100; i++) {
				long lifetime = Util.getRandom().nextInt(2000) + 1000;
				Color randomOneOf = Util.randomOneOf(color);
				Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 400f, 0f).rotate(Util.getRandom().nextFloat() * 360f);

				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					rotate,
					Color.WHITE,
					randomOneOf,
					Color.WHITE,
					Util.getRandom().nextFloat() * 5f,
					Domain.getActiveWorld().getWorldId(),
					Util.getRandom().nextFloat() * 10f,
					Util.randomOneOf(MovementMode.WEIGHTLESS),
					Util.getRandom().nextBoolean() ? Depth.FOREGROUND : Depth.MIDDLEGROUND,
					lifetime,
					true
				).bounce());
			}
		}
	}


	public static void randomVelocityTracer(Vector2 position, float spawnSpread, float maxVel, Color color, Color glowColor, float glow, int maxLifeTime, MovementMode mode, Depth depth) {
		if (isClient()) {
			Domain.getActiveWorld().getClientParticles().add(new TracerParticle(
				position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
				new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
				color,
				glowColor,
				1f,
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(maxLifeTime)),
				glow,
				mode,
				depth
			));
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new FlameEmber(position.cpy(), spawnSpread, maxVel, new SerializableColor(color), new SerializableColor(glowColor), glow, maxLifeTime, mode, depth));
		}
	}



	public static void randomVelocityTextureBackedParticle(Vector2 position, float spawnSpread, float maxVel, Color color, int maxLifeTime, MovementMode mode, Depth depth) {
		if (isClient()) {
			Domain.getActiveWorld().getClientParticles().add(new TextureBackedParticle(
				position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
				new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
				color,
				Util.getRandom().nextFloat() * 2f,
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(maxLifeTime)),
				mode,
				depth,
				0.1f
			));
		}
	}


	public static void randomVelocityDiminishing(Vector2 position, float spawnSpread, float maxVel, Color color, Color glowColor, float initialRadius, float glow, MovementMode mode, long diminishingDuration, Depth depth, boolean tracer, Color toChangeTo) {
		if (diminishingDuration < 100) {
			diminishingDuration = 100;
		}

		if (isClient()) {
			if (tracer) {
				Domain.getActiveWorld().getClientParticles().add(new DiminishingTracerParticle(
					position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
					new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
					color,
					glowColor,
					initialRadius,
					Domain.getActiveWorld().getWorldId(),
					glow,
					mode,
					depth,
					diminishingDuration
				));
			} else {
				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
					new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
					color,
					glowColor,
					toChangeTo,
					initialRadius,
					Domain.getActiveWorld().getWorldId(),
					glow,
					mode,
					depth,
					diminishingDuration,
					tracer
				));
			}

		}
	}


	public static void parrySpark(Vector2 position, Vector2 knockBack, Depth depth, Color color, Color glowColor, int life, boolean trancer, int numberOfParticles, float baseSpeed) {
		if (isClient()) {
			for (int i = 0; i < numberOfParticles; i++) {
				long lifetime = Util.getRandom().nextInt(life);
				float size = Util.getRandom().nextFloat();
				Domain.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * baseSpeed, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).scl(2f),
					color,
					glowColor,
					color,
					size * 1.2f,
					Domain.getActiveWorld().getWorldId(),
					size * 5f,
					MovementMode.GRAVITY,
					depth,
					lifetime,
					trancer
				).bounce());
			}
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new ParrySpark(position.cpy(), knockBack, depth));
		}
	}


	public static class ParrySpark implements Runnable, Serializable {
		private static final long serialVersionUID = -7518924240885920128L;
		private Vector2 position;
		private Vector2 knockBack;
		private Depth depth;

		public ParrySpark(Vector2 position, Vector2 knockBack, Depth depth) {
			this.position = position;
			this.knockBack = knockBack;
			this.depth = depth;
		}

		@Override
		public void run() {
			ParticleService.parrySpark(position, knockBack, depth, Color.WHITE, Color.WHITE, 100, true, 30, 200f);
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
		private MovementMode mode;
		private float maxVel;
		private float spawnSpread;
		private int maxLifeTime;
		private Depth depth;
		private SerializableColor glowColor;

		public FlameEmber(Vector2 position, float spawnSpread, float maxVel, SerializableColor color, SerializableColor glowColor, float glow, int maxLifeTime, MovementMode mode, Depth depth) {
			this.position = position;
			this.spawnSpread = spawnSpread;
			this.maxVel = maxVel;
			this.color = color;
			this.glowColor = glowColor;
			this.glow = glow;
			this.maxLifeTime = maxLifeTime;
			this.mode = mode;
			this.depth = depth;
		}

		@Override
		public void run() {
			ParticleService.randomVelocityTracer(position, spawnSpread, maxVel, color.getColor(), glowColor.getColor(), glow, maxLifeTime, mode, depth);
		}
	}
}