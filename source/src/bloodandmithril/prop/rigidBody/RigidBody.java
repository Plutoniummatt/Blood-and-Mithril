package bloodandmithril.prop.rigidBody;

import bloodandmithril.character.Individual;
import bloodandmithril.prop.Prop;
import bloodandmithril.world.Domain.Depth;

/**
 * A rigid body, able to move, bounce, and possibly collide with other collidable {@link RigidBody}s
 *
 * @author Matt
 */
public abstract class RigidBody extends Prop {
	private static final long serialVersionUID = 2944768397395915663L;

	/** True if this {@link RigidBody} can collide with other Collidable {@link RigidBody}s as well as {@link Individual}s */
	public boolean collidable;

	/** The collision radius */
	public float collisionRadius;

	/** If frozen, this {@link RigidBody} will not be eligible for {@link #update(float)} */
	public boolean frozen;

	/**
	 * Constructor
	 */
	protected RigidBody(float x, float y, boolean collidable, float collisionRadius) {
		super(x, y, false, Depth.FOREGOUND);
		this.collidable = collidable;
		this.collisionRadius = collisionRadius;
	}


	/**
	 * Kinetics of this {@link RigidBody}
	 */
	public void update() {
		//TODO
	}
}