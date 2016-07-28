package bloodandmithril.graphics.particles;

import static bloodandmithril.networking.ClientServerInterface.isClient;

import java.io.Serializable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;

/**
 * Service responsible for adding particles to the world
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class ParticleService {

	private static GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

	public static void bloodSplat(final Vector2 position, final Vector2 knockBack) {
		if (isClient()) {
			for (int i = 0; i < 35; i++) {
				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 40f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).scl(5f),
					Color.RED,
					Color.RED,
					Color.RED,
					2f,
					gameClientStateTracker.getActiveWorld().getWorldId(),
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


	public static void mineExplosion(final Vector2 position, final Color c) {
		if (isClient()) {
			for (int i = 0; i < 40; i++) {
				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy().add(
						(Util.getRandom().nextFloat() - 0.5f) * 30,
						(Util.getRandom().nextFloat() - 0.5f) * 30
					),
					new Vector2(Util.getRandom().nextFloat() * 30f, 0f).rotate(Util.getRandom().nextFloat() * 360f).scl(5f),
					Colors.LIGHT_SMOKE,
					Colors.LIGHT_SMOKE,
					Colors.LIGHT_SMOKE,
					Util.getRandom().nextFloat() * 25f + 3f,
					gameClientStateTracker.getActiveWorld().getWorldId(),
					0f,
					MovementMode.WEIGHTLESS,
					Depth.FOREGROUND,
					Util.getRandom().nextInt(5000) + 1000,
					false
				));
			}
			for (int i = 0; i < 25; i++) {
				final float radius = Util.getRandom().nextFloat() * 3f;
				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 50f, 0f).rotate(Util.getRandom().nextFloat() * 360f).scl(5f),
					c,
					c,
					c,
					radius,
					gameClientStateTracker.getActiveWorld().getWorldId(),
					0,
					MovementMode.GRAVITY,
					Depth.FOREGROUND,
					Util.getRandom().nextInt(500),
					true
				));
			}
		}
	}


	public static void fireworks(final Vector2 position, final Color... color) {
		if (isClient()) {
			for (int i = 0; i < 100; i++) {
				final long lifetime = Util.getRandom().nextInt(2000) + 1000;
				final Color randomOneOf = Util.randomOneOf(color);
				final Vector2 rotate = new Vector2(Util.getRandom().nextFloat() * 400f, 0f).rotate(Util.getRandom().nextFloat() * 360f);

				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					rotate,
					Color.WHITE,
					randomOneOf,
					Color.WHITE,
					Util.getRandom().nextFloat() * 5f,
					gameClientStateTracker.getActiveWorld().getWorldId(),
					Util.getRandom().nextFloat() * 10f,
					Util.randomOneOf(MovementMode.WEIGHTLESS),
					Util.getRandom().nextBoolean() ? Depth.FOREGROUND : Depth.MIDDLEGROUND,
					lifetime,
					true
				).bounce());
			}
		}
	}


	public static void randomVelocityTracer(final Vector2 position, final float spawnSpread, final float maxVel, final Color color, final Color glowColor, final float glow, final int maxLifeTime, final MovementMode mode, final Depth depth) {
		if (isClient()) {
			gameClientStateTracker.getActiveWorld().getClientParticles().add(new TracerParticle(
				position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
				new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
				color,
				glowColor,
				1f,
				gameClientStateTracker.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(maxLifeTime)),
				glow,
				mode,
				depth
			));
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new FlameEmber(position.cpy(), spawnSpread, maxVel, new SerializableColor(color), new SerializableColor(glowColor), glow, maxLifeTime, mode, depth));
		}
	}



	public static void randomVelocityTextureBackedParticle(final Vector2 position, final float spawnSpread, final float maxVel, final Color color, final int maxLifeTime, final MovementMode mode, final Depth depth) {
		if (isClient()) {
			gameClientStateTracker.getActiveWorld().getClientParticles().add(new TextureBackedParticle(
				position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
				new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
				color,
				Util.getRandom().nextFloat() * 2f,
				gameClientStateTracker.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(maxLifeTime)),
				mode,
				depth,
				0.1f
			));
		}
	}


	public static void randomVelocityDiminishing(final Vector2 position, final float spawnSpread, final float maxVel, final Color color, final Color glowColor, final float initialRadius, final float glow, final MovementMode mode, long diminishingDuration, final Depth depth, final boolean tracer, final Color toChangeTo) {
		if (diminishingDuration < 100) {
			diminishingDuration = 100;
		}

		if (isClient()) {
			if (tracer) {
				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingTracerParticle(
					position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
					new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
					color,
					glowColor,
					initialRadius,
					gameClientStateTracker.getActiveWorld().getWorldId(),
					glow,
					mode,
					depth,
					diminishingDuration
				));
			} else {
				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
					new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
					color,
					glowColor,
					toChangeTo,
					initialRadius,
					gameClientStateTracker.getActiveWorld().getWorldId(),
					glow,
					mode,
					depth,
					diminishingDuration,
					tracer
				));
			}

		}
	}


	public static void parrySpark(final Vector2 position, final Vector2 knockBack, final Depth depth, final Color color, final Color glowColor, final int life, final boolean trancer, final int numberOfParticles, final float baseSpeed) {
		if (isClient()) {
			for (int i = 0; i < numberOfParticles; i++) {
				final long lifetime = Util.getRandom().nextInt(life);
				final float size = Util.getRandom().nextFloat();
				gameClientStateTracker.getActiveWorld().getClientParticles().add(new DiminishingColorChangingParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * baseSpeed, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).scl(2f),
					color,
					glowColor,
					color,
					size * 1.2f,
					gameClientStateTracker.getActiveWorld().getWorldId(),
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

		public ParrySpark(final Vector2 position, final Vector2 knockBack, final Depth depth) {
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

		public BloodSplat(final Vector2 position, final Vector2 knockBack) {
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

		public FlameEmber(final Vector2 position, final float spawnSpread, final float maxVel, final SerializableColor color, final SerializableColor glowColor, final float glow, final int maxLifeTime, final MovementMode mode, final Depth depth) {
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