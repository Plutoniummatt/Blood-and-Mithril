package bloodandmithril.prop;

import java.io.Serializable;

import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.building.Furnace;
import bloodandmithril.prop.crafting.Anvil;
import bloodandmithril.prop.crafting.WorkBench;
import bloodandmithril.prop.furniture.WoodenChest;
import bloodandmithril.prop.plant.Carrot;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * A renderable {@link Prop}
 *
 * @author Matt
 */
public abstract class Prop implements Serializable {
	private static final long serialVersionUID = -1659783923740689585L;

	/** Whether or not this prop will be rendered as part of the background */
	public final Depth depth;

	/** id of this prop */
	public int id;

	/** The location of this {@link Prop} */
	public Vector2 position;

	/** True if this {@link Prop} must be placed on the ground */
	protected final boolean grounded;

	/**
	 * Constructor
	 */
	protected Prop(float x, float y, boolean grounded, Depth depth) {
		position = new Vector2(x, y);
		this.depth = depth;
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
	public abstract void synchronizeProp(Prop other);

	/** Get the right-click {@link ContextMenu} */
	public abstract ContextMenu getContextMenu();

	/** Updates this prop */
	public abstract void update(float delta);

	/** Returns the label to use for the right click context menu */
	public abstract String getContextMenuItemLabel();

	public static void setup() {
		Furnace.FURANCE = new TextureRegion(Domain.gameWorldTexture, 453, 176, 49, 76);
		Furnace.FURNACE_BURNING = new TextureRegion(Domain.gameWorldTexture, 503, 176, 49, 76);
		WoodenChest.woodenChest = new TextureRegion(Domain.gameWorldTexture, 408, 206, 44, 35);
		Carrot.carrot = new TextureRegion(Domain.gameWorldTexture, 352, 176, 12, 17);
		Anvil.anvil = new TextureRegion(Domain.gameWorldTexture, 363, 225, 44, 18);
		WorkBench.workbench = new TextureRegion(Domain.gameWorldTexture, 363, 225, 44, 18);
	}
}
