package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.TracerParticle;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.util.Countdown;
import bloodandmithril.util.SerializableColor;
import bloodandmithril.util.Util;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class FlareArrow<T extends Metal> extends Arrow<T> {
	private static final long serialVersionUID = -6641284233913835594L;
	private float lightingDuration;
	private SerializableColor color;
	private Vector2 previousPosition;
	private transient TracerParticle particle;

	/**
	 * Constructor
	 */
	public FlareArrow(Class<T> metal, Vector2 position, Vector2 velocity, float lightingDuration, Color color) {
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
					particle = new TracerParticle(position, velocity, color.getColor(), 2f, getWorldId(), new Countdown(Util.getRandom().nextInt(Integer.MAX_VALUE)), 20f, MovementMode.WEIGHTLESS, false);
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


	public static class FlareArrowItem<T extends Metal> extends ArrowItem<T> {
		private static final long serialVersionUID = 9027137493687956507L;

		public FlareArrowItem(Class<T> metal, long value) {
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
			return super.getDescription() + ", this one glows.";
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(Item other) {
			if (other instanceof FlareArrowItem) {
				return metal.equals(((FlareArrowItem) other).metal);
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
			return new FlareArrowItem<>(metal, getValue());
		}


		@Override
		public Projectile getProjectile() {
			return new FlareArrow<>(metal, null, null, 30f, Color.GREEN);
		}
	}
}