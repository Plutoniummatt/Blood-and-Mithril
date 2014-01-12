package bloodandmithril.prop;


import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.ui.components.ContextMenu;

import com.badlogic.gdx.math.Vector2;

/**
 * A renderable {@link Prop}
 *
 * @author Matt
 */
public abstract class Prop {

	/** id of this prop */
	public int id;

	/** The location of this {@link Prop} */
	public Vector2 position;

	/** True if this {@link Prop} must be placed on the ground */
	protected final boolean grounded;

	/**
	 * Constructor
	 */
	protected Prop(float x, float y, boolean grounded) {
		position = new Vector2(x, y);
		this.id = ParameterPersistenceService.getParameters().getNextPropId();
		this.grounded = grounded;
	}

	/** Render this {@link Prop} */
	public abstract void render();

	/** True if mouse is over this {@link Prop} */
	public abstract boolean isMouseOver();

	/** Called when this {@link Prop} has been left clicked */
	public abstract boolean leftClick();

	/** Called when this {@link Prop} has been right clicked */
	public abstract boolean rightClick();

	/** Get the right-click {@link ContextMenu} */
	public abstract ContextMenu getContextMenu();
}
