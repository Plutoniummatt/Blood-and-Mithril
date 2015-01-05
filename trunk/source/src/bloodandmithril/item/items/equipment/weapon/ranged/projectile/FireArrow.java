package bloodandmithril.item.items.equipment.weapon.ranged.projectile;

import bloodandmithril.character.conditions.Burning;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.particles.Particle.MovementMode;
import bloodandmithril.graphics.particles.ParticleService;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.Projectile;
import bloodandmithril.item.material.metal.Metal;
import bloodandmithril.util.Util;

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
			ParticleService.randomVelocityDiminishing(position, 0f, 30f, Color.ORANGE, Util.getRandom().nextFloat() * 3f, 5f, MovementMode.EMBER, Util.getRandom().nextInt(300), false);
			ParticleService.randomVelocityDiminishing(position, 0f, 30f, Color.ORANGE, Util.getRandom().nextFloat() * 2f, 5f, MovementMode.EMBER, Util.getRandom().nextInt(600), false);
			ParticleService.randomVelocityDiminishing(position, 0f, 30f, Color.GRAY, 1f, 0f, MovementMode.EMBER, Util.getRandom().nextInt(1000), false);
		}

		super.update(delta);
	}


	@Override
	public void hit(Individual victim) {
		super.hit(victim);

		if (Util.roll(0.35f)) {
			victim.addCondition(new Burning(10f));
		}
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