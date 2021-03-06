package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import static bloodandmithril.networking.ClientServerInterface.isServer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.RenderProjectileWith;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.graphics.particles.DiminishingTracerParticle;
import bloodandmithril.graphics.particles.Particle;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.renderers.ArrowRenderer;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

@Copyright("Matthew Peck 2016")
@RenderProjectileWith(ArrowRenderer.class)
public class GlowStickArrowProjectile<T extends Metal> extends ArrowProjectile<T> {
	private static final long serialVersionUID = -6641284233913835594L;
	private float lightingDuration;
	private SerializableColor color;
	private Vector2 previousPosition;
	private Long particleId;

	/**
	 * Constructor
	 */
	public GlowStickArrowProjectile(final Class<T> metal, final Vector2 position, final Vector2 velocity, final float lightingDuration, final Color color) {
		super(metal, position, velocity);
		this.lightingDuration = lightingDuration;
		this.previousPosition = position;
		this.color = new SerializableColor(color);
	}


	@Override
	public void particleEffects(final float delta) {
		if (lightingDuration > 0f) {
			lightingDuration -= delta;
			if (particleId == null) {
				if (isServer()) {
					final Particle particle = new DiminishingTracerParticle(
						position,
						velocity,
						Color.WHITE,
						color.getColor(),
						2f,
						getWorldId(),
						10f,
						MovementMode.WEIGHTLESS,
						Depth.FOREGROUND,
						(long) lightingDuration * 1000
					);
					particle.doNotUpdate();
					Domain.getWorld(getWorldId()).getServerParticles().put(particle.particleId, particle);
					particleId = particle.particleId;
				}
			} else {
				final DiminishingTracerParticle particle = (DiminishingTracerParticle) Domain.getWorld(getWorldId()).getServerParticles().get(particleId);
				if (particle != null) {
					particle.position = position;
					particle.prevPosition = previousPosition;
				}
			}
		} else {
			if (ClientServerInterface.isServer()) {
				Domain.getWorld(getWorldId()).getServerParticles().remove(particleId);
			}
		}

		previousPosition = position.cpy();
	}


	@Override
	public void targetHitKinematics() {
		if (Util.roll(0.2f)) {
			Domain.getWorld(getWorldId()).projectiles().removeProjectile(getId());
			Domain.getWorld(getWorldId()).getServerParticles().remove(particleId);
		} else {
			velocity.scl(0.05f);
			velocity.x = -velocity.x;
		}
	}


	public static class GlowStickArrowItem<T extends Metal> extends ArrowItem<T> {
		private static final long serialVersionUID = 9027137493687956507L;

		public GlowStickArrowItem(final Class<T> metal) {
			super(metal);
			this.setValue(getValue() + ItemValues.GLOWSTICK);
		}

		@Override
		protected String internalGetSingular(final boolean firstCap) {
			return "Glowing " + super.internalGetSingular(firstCap);
		}


		@Override
		protected String internalGetPlural(final boolean firstCap) {
			return "Glowing " + super.internalGetPlural(firstCap);
		}


		@Override
		public String getDescription() {
			return super.getDescription() + ", this one has a glowstick attached to it.";
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(final Item other) {
			if (other instanceof GlowStickArrowItem) {
				return metal.equals(((GlowStickArrowItem) other).metal);
			}
			return false;
		}


		@Override
		public TextureRegion getTextureRegion() {
			return ArrowProjectile.textureRegion;
		}


		@Override
		public TextureRegion getIconTextureRegion() {
			return null;
		}


		@Override
		protected Item internalCopy() {
			return new GlowStickArrowItem<>(metal);
		}


		@Override
		public Projectile getProjectile() {
			return new GlowStickArrowProjectile<>(metal, null, null, 10f, new Color(0.5f, 1f, 1f, 1f));
		}
	}
}