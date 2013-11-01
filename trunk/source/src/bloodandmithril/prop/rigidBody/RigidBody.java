package bloodandmithril.prop.rigidBody;

import bloodandmithril.prop.Prop;

/**
 * A rigid body, able to move, bounce, and possibly collide with other collidable {@link RigidBody}s
 *
 * @author Matt
 */
public abstract class RigidBody extends Prop {

	/** True if this {@link RigidBody} can collide with other Collidable {@link RigidBody}s */
	public boolean collidable;

	/** The collision radius */
	public float collisionRadius;

	/** If frozen, this {@link RigidBody} will not be eligible for {@link #update(float)} */
	public boolean frozen;

	/**
	 * Constructor
	 */
	protected RigidBody(float x, float y, boolean collidable, float collisionRadius) {
		super(x, y, false);
		this.collidable = collidable;
		this.collisionRadius = collisionRadius;
	}


	/**
	 * Kinetics of this {@link RigidBody}
	 */
	public void update(float delta) {
		//TODO
	}
}