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
import bloodandmithril.world.Domain.Depth;

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
					new Vector2(Util.getRandom().nextFloat() * 40f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).mul(5f),
					Color.RED,
					Color.RED,
					2f,
					Domain.getActiveWorld().getWorldId(),
					0f,
					MovementMode.GRAVITY,
					Depth.FOREGOUND,
					Util.getRandom().nextInt(3000)
				));
			}
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new BloodSplat(position.cpy(), knockBack));
		}
	}


	public static void randomVelocityTracer(Vector2 position, float spawnSpread, float maxVel, Color color, float glow, int maxLifeTime, MovementMode mode, Depth depth) {
		if (isClient()) {
			Domain.getActiveWorld().getClientParticles().add(new TracerParticle(
				position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
				new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
				color,
				1f,
				Domain.getActiveWorld().getWorldId(),
				new Countdown(Util.getRandom().nextInt(maxLifeTime)),
				glow,
				mode,
				depth
			));
		} else {
			ClientServerInterface.SendNotification.notifyRunStaticMethod(-1, new FlameEmber(position.cpy(), spawnSpread, maxVel, new SerializableColor(color), glow, maxLifeTime, mode, depth));
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


	public static void randomVelocityDiminishing(Vector2 position, float spawnSpread, float maxVel, Color color, float initialRadius, float glow, MovementMode mode, long diminishingDuration, Depth depth, boolean tracer, Color toChangeTo) {
		if (diminishingDuration < 100) {
			diminishingDuration = 100;
		}
		
		if (isClient()) {
			if (tracer) {
				Domain.getActiveWorld().getClientParticles().add(new DiminishingTracerParticle(
					position.cpy().add(new Vector2(Util.getRandom().nextFloat() * spawnSpread, 0f).rotate(Util.getRandom().nextFloat() * 360f)),
					new Vector2(Util.getRandom().nextFloat() * maxVel, 0f).rotate(Util.getRandom().nextFloat() * 360f),
					color,
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
					toChangeTo,
					initialRadius,
					Domain.getActiveWorld().getWorldId(),
					glow,
					mode,
					depth,
					diminishingDuration
				));
			}

		}
	}


	public static void parrySpark(Vector2 position, Vector2 knockBack, Depth depth, Color color, int life) {
		if (isClient()) {
			for (int i = 0; i < 35; i++) {
				Domain.getActiveWorld().getClientParticles().add(new TracerParticle(
					position.cpy(),
					new Vector2(Util.getRandom().nextFloat() * 200f, 0f).rotate(Util.getRandom().nextFloat() * 360f).add(knockBack).mul(2f),
					color,
					1f,
					Domain.getActiveWorld().getWorldId(),
					new Countdown(Util.getRandom().nextInt(life)),
					5f,
					MovementMode.GRAVITY,
					depth
				));
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
			ParticleService.parrySpark(position, knockBack, depth, Color.WHITE, 100);
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

		public FlameEmber(Vector2 position, float spawnSpread, float maxVel, SerializableColor color, float glow, int maxLifeTime, MovementMode mode, Depth depth) {
			this.position = position;
			this.spawnSpread = spawnSpread;
			this.maxVel = maxVel;
			this.color = color;
			this.glow = glow;
			this.maxLifeTime = maxLifeTime;
			this.mode = mode;
			this.depth = depth;
		}

		@Override
		public void run() {
			ParticleService.randomVelocityTracer(position, spawnSpread, maxVel, color.getColor(), glow, maxLifeTime, mode, depth);
		}
	}
}