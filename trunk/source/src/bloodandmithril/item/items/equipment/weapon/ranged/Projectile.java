package bloodandmithril.item.items.equipment.weapon.ranged;

import com.badlogic.gdx.math.Vector2;

/**
 * Class representing a projectile
 *
 * @author Matt
 */
public abstract class Projectile {
	public Vector2 position, velocity, acceleration;
	
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
	public abstract void update();
}