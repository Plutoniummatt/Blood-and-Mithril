package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * A flaming arrow
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class FireArrow<T extends Metal> extends Arrow<T> {
	private static final long serialVersionUID = -4345906143614035570L;
	private float burnDuration;

	/**
	 * Constructor
	 */
	public FireArrow(Class<T> metal, Vector2 position, Vector2 velocity, float burnDuration) {
		super(metal, position, velocity);
		this.burnDuration = burnDuration;
	}


	@Override
	public void update(float delta) {
		if (burnDuration > 0f) {
			burnDuration -= delta;
			ParticleService.randomVelocity(position, 0f, 30f, Color.ORANGE, 10f, 100, MovementMode.EMBER);
			ParticleService.randomVelocity(position, 0f, 30f, Color.ORANGE, 10f, 400, MovementMode.EMBER);
			ParticleService.randomVelocity(position, 0f, 30f, Color.GRAY, 0f, 1000, MovementMode.EMBER);
		}

		super.update(delta);
	}


	public static class FireArrowItem<T extends Metal> extends ArrowItem<T> {
		private static final long serialVersionUID = 9027137493687956507L;

		public FireArrowItem(Class<T> metal, long value) {
			super(metal, value);
		}

		@Override
		protected String internalGetSingular(boolean firstCap) {
			return "Burning " + super.internalGetSingular(firstCap);
		}


		@Override
		protected String internalGetPlural(boolean firstCap) {
			return "Burning " + super.internalGetPlural(firstCap);
		}


		@Override
		public String getDescription() {
			return super.getDescription() + ", this one can be lit.";
		}


		@Override
		@SuppressWarnings("rawtypes")
		protected boolean internalSameAs(Item other) {
			if (other instanceof FireArrowItem) {
				return metal.equals(((FireArrowItem) other).metal);
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
			return new FireArrowItem<>(metal, getValue());
		}


		@Override
		public Projectile getProjectile() {
			return new FireArrow<>(metal, null, null, 10f);
		}
	}
}