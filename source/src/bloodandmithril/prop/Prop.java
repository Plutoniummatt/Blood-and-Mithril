package bloodandmithril.prop;


import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.building.PineChest;
import bloodandmithril.prop.plant.Carrot;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.world.GameWorld;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * A renderable {@link Prop}
 *
 * @author Matt
 */
public abstract class Prop {

	/** Whether or not this prop will be rendered as part of the background */
	public final boolean backGround;

	/** id of this prop */
	public int id;

	/** The location of this {@link Prop} */
	public Vector2 position;

	/** True if this {@link Prop} must be placed on the ground */
	protected final boolean grounded;

	/**
	 * Constructor
	 */
	protected Prop(float x, float y, boolean grounded, boolean backGround) {
		position = new Vector2(x, y);
		this.backGround = backGround;
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

	/** Synchronizes this prop with another */
	public abstract void synchronize(Prop other);

	/** Get the right-click {@link ContextMenu} */
	public abstract ContextMenu getContextMenu();

	public static void setup() {
		PineChest.pineChest = new TextureRegion(GameWorld.gameWorldTexture, 350, 175, 57, 68);
		Carrot.carrot = new TextureRegion(GameWorld.gameWorldTexture, 350, 175, 16, 24);
	}
}
