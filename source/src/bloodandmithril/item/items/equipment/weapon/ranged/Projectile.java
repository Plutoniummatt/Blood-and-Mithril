package bloodandmithril.item.items.equipment.weapon.ranged;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.items.equipment.weapon.ranged.projectile.Arrow;
import bloodandmithril.performance.PositionalIndexNode;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Sets;

/**
 * Class representing a projectile
 *
 * @author Matt
 */
public abstract class Projectile implements Serializable {
	private static final long serialVersionUID = -6124242148644632575L;

	public Vector2 position, velocity, acceleration;
	private int worldId, id;
	private Set<Integer> ignoredIndividuals = Sets.newHashSet();
	
	static {
		Arrow.textureRegion = new TextureRegion(Domain.gameWorldTexture, 619, 176, 50, 3);
	}

	/**
	 * Protected constructor
	 */
	protected Projectile(Vector2 position, Vector2 velocity, Vector2 acceleration) {
		this.position = position;
		this.velocity = velocity;
		this.acceleration = acceleration;
	}

	/**
	 * Renders this {@link Projectile}
	 */
	public abstract void render();
	
	
	/**
	 * @param called when this {@link Projectile} hits the victim
	 */
	public abstract void hit(Individual victim);


	/**
	 * Updates this {@link Projectile}
	 */
	public void update(float delta) {
		Optional<Integer> findAny = Domain.getWorld(getWorldId()).getPositionalIndexMap().getNearbyEntities(Individual.class, position).stream().filter(individual -> {
			return Domain.getIndividual(individual).getHitBox().isWithinBox(position);
		}).findAny();
		
		if (findAny.isPresent()) {
			Individual individual = Domain.getIndividual(findAny.get());
			if (canAffect(individual)) {
				hit(individual);
				ignoreIndividual(individual);
				if (!penetrating()) {
					Domain.getWorld(getWorldId()).projectiles().removeProjectile(getId());
				}
			}
		}
		
		if (!Domain.getWorld(getWorldId()).getTopography().getChunkMap().doesChunkExist(position)) {
			return;
		}
		Vector2 previousPosition = position.cpy();
		position.add(velocity.cpy().mul(delta));
		float gravity = Domain.getWorld(getWorldId()).getGravity();
		if (velocity.len() > getTerminalVelocity()) {
			velocity.add(0f, -gravity * delta).mul(0.95f);
		} else {
			velocity.add(0f, -gravity * delta);
		}
		
		Tile tileUnder = Domain.getWorld(getWorldId()).getTopography().getTile(position.x, position.y, true);
		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			collision(previousPosition);
		}

		updatePositionIndex();
	}
	
	protected abstract void collision(Vector2 previousPosition);

	protected abstract float getTerminalVelocity();
	
	protected abstract boolean penetrating();
	
	public void ignoreIndividual(Individual individual) {
		ignoredIndividuals.add(individual.getId().getId());
	}
	
	private boolean canAffect(Individual individual) {
		return !ignoredIndividuals.contains(individual.getId().getId());
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}


	public int getWorldId() {
		return worldId;
	}


	public void setId(int nextProjectileId) {
		this.id = nextProjectileId;
	}


	public void setPosition(Vector2 position) {
		this.position = position;
	}


	public void setVelocity(Vector2 velocity) {
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


	public void updatePositionIndex() {
		for (PositionalIndexNode node : Domain.getWorld(worldId).getPositionalIndexMap().getNearbyNodes(position.x, position.y)) {
			node.removeProjectile(id);
		}

		Domain.getWorld(worldId).getPositionalIndexMap().get(position.x, position.y).addProjectile(id);
	}
}