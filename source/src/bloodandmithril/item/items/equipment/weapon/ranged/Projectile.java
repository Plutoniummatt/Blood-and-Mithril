package bloodandmithril.item.items.equipment.weapon.ranged;

import java.io.Serializable;
import java.util.Set;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.graphics.Textures;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.ArrowProjectile;
import bloodandmithril.networking.ClientServerInterface;

/**
 * Class representing a projectile
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2016")
public abstract class Projectile implements Serializable {
	private static final long serialVersionUID = -6124242148644632575L;

	public Vector2 position;
	public Vector2 pPosition;
	public Vector2 velocity;
	public Vector2 acceleration;

	private int worldId, id;
	protected boolean stuck = false;
	private Set<Integer> ignoredIndividuals = Sets.newHashSet();

	static {
		if (ClientServerInterface.isClient()) {
			ArrowProjectile.textureRegion = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 619, 176, 50, 3);
		}
	}

	/**
	 * Protected constructor
	 */
	protected Projectile(final Vector2 position, final Vector2 velocity, final Vector2 acceleration) {
		this.position = position;
		this.pPosition = position;
		this.velocity = velocity;
		this.acceleration = acceleration;
	}

	/**
	 * @param called when this {@link Projectile} hits the victim
	 */
	public abstract void hit(Individual victim);

	/**
	 * Called when an {@link Individual} is hit
	 */
	public abstract void targetHitKinematics();

	/**
	 * @return the hit sound when it hits an {@link Individual}
	 */
	public abstract int getHitSound(Individual individual);

	/**
	 * Called when this projectile collides with a tile
	 */
	public abstract void collision(Vector2 previousPosition);

	/**
	 * @return The terminal velocity of this {@link Projectile}
	 */
	public abstract float getTerminalVelocity();

	/**
	 * @return whether this {@link Projectile} will penetrate
	 */
	public abstract boolean penetrating();

	/**
	 * Adds particles
	 */
	public abstract void particleEffects(float delta);

	/**
	 * called just before the projectile is fired.
	 */
	public abstract void preFireDecorate(Individual individual);

	/**
	 * @param individual to be ignored by this {@link Projectile}
	 */
	public void ignoreIndividual(final Individual individual) {
		ignoredIndividuals.add(individual.getId().getId());
	}

	public boolean canAffect(final Individual individual) {
		return !ignoredIndividuals.contains(individual.getId().getId()) && individual.isAlive();
	}

	public void setWorldId(final int worldId) {
		this.worldId = worldId;
	}


	public int getWorldId() {
		return worldId;
	}


	public void setId(final int nextProjectileId) {
		this.id = nextProjectileId;
	}


	public void setPosition(final Vector2 position) {
		if (pPosition == null) {
			pPosition = position.cpy();
		}
		this.position = position;
	}


	public void setVelocity(final Vector2 velocity) {
		this.velocity = velocity;
	}


	public Vector2 getPosition() {
		return position;
	}


	public Vector2 getVelocity() {
		return velocity;
	}


	public Integer getId() {
		return id;
	}


	public boolean isStuck() {
		return stuck;
	}


	public static abstract class ProjectileItem extends Item {
		private static final long serialVersionUID = -8997843179593339141L;

		/**
		 * Constructor
		 */
		protected ProjectileItem(final float mass, final int volume, final boolean equippable, final long value) {
			super(mass, volume, equippable, value);
		}


		@Override
		public ItemCategory getType() {
			return ItemCategory.AMMO;
		}

		public abstract Projectile getProjectile();

		@Override
		public float getUprightAngle() {
			return 0f;
		}
	}
}