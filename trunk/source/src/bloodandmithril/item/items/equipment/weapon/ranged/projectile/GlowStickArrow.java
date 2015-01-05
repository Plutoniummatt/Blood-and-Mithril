package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.graphics.particles.DiminishingTracerParticle;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class GlowStickArrow<T extends Metal> extends Arrow<T> {
	private static final long serialVersionUID = -6641284233913835594L;
	private float lightingDuration;
	private SerializableColor color;
	private Vector2 previousPosition;
	private transient TracerParticle particle;

	/**
	 * Constructor
	 */
	public GlowStickArrow(Class<T> metal, Vector2 position, Vector2 velocity, float lightingDuration, Color color) {
		super(metal, position, velocity);
		this.lightingDuration = lightingDuration;
		this.previousPosition = position;
		this.color = new SerializableColor(color);
	}


	@Override
	public void update(float delta) {
		if (lightingDuration > 0f) {
			lightingDuration -= delta;
			if (ClientServerInterface.isClient()) {
				if (particle == null) {
					particle = new DiminishingTracerParticle(
						position,
						velocity,
						color.getColor(),
						2f,
						getWorldId(),
						15f,
						MovementMode.WEIGHTLESS,
						false,
						(long) lightingDuration * 1000
					);
					particle.doNotUpdate();
					Domain.getWorld(getWorldId()).getParticles().add(particle);
				} else {
					particle.position = position;
					particle.prevPosition = previousPosition;
				}
			}
		} else {
			Domain.getWorld(getWorldId()).getParticles().remove(particle);
		}

		previousPosition = position.cpy();
		super.update(delta);
	}


	@Override
	protected void targetHitKinematics() {
		if (Util.roll(0.2f)) {
			Domain.getWorld(getWorldId()).projectiles().removeProjectile(getId());
			Domain.getWorld(getWorldId()).getParticles().remove(particle);
		} else {
			velocity.mul(0.05f);
			velocity.x = -velocity.x;
		}
	}


	public static class GlowStickArrowItem<T extends Metal> extends ArrowItem<T> {
		private static final long serialVersionUID = 9027137493687956507L;

		public GlowStickArrowItem(Class<T> metal, long value) {
			super(metal, value);
		}

		@Override
		protected String internalGetSingular(boolean firstCap) {
			return "Glowing " + super.internalGetSingular(firstCap);
		}


		@Override
		protected String internalGetPlural(boolean firstCap) {
			return "Glowing " + super.internalGetPlural(firstCap);
		}


		@Override
		public String getDescription() {
			return super.getDescription() + ", this one has a glowstick attached to it.";
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(Item other) {
			if (other instanceof GlowStickArrowItem) {
				return metal.equals(((GlowStickArrowItem) other).metal);
			}
			return false;
		}


		@Override
		public TextureRegion getTextureRegion() {
			return null;
		}


		@Override
		public TextureRegion getIconTextureRegion() {
			return null;
		}


		@Override
		protected Item internalCopy() {
			return new GlowStickArrowItem<>(metal, getValue());
		}


		@Override
		public Projectile getProjectile() {
			return new GlowStickArrow<>(metal, null, null, 30f, new Color(0f, 1f, 0f, 1f));
		}
	}
}