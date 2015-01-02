package bloodandmithril.item.items.equipment.weapon.ranged;

import java.io.Serializable;

import bloodandmithril.performance.PositionalIndexNode;
import bloodandmithril.world.Domain;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a projectile
 *
 * @author Matt
 */
public abstract class Projectile implements Serializable {
	private static final long serialVersionUID = -6124242148644632575L;

	public Vector2 position, velocity, acceleration;
	private int worldId, id;

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
	 * Updates this {@link Projectile}
	 */
	public void update(float delta) {
		position.add(velocity.cpy().mul(delta));
		float gravity = Domain.getWorld(getWorldId()).getGravity();
		if (velocity.len() > getTerminalVelocity()) {
			velocity.add(0f, -gravity * delta).mul(0.95f);
		} else {
			velocity.add(0f, -gravity * delta);
		}

		updatePositionIndex();
	}


	protected abstract float getTerminalVelocity();


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