package bloodandmithril.prop;

import java.io.Serializable;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.construction.craftingstation.Anvil;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.WoodenChest;
import bloodandmithril.prop.plant.CarrotProp;
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
@Copyright("Matthew Peck 2014")
public abstract class Prop implements Serializable {
	private static final long serialVersionUID = -1659783923740689585L;

	/** Dimensions of this {@link Prop} */
	public final int width, height;

	/** Whether this prop will be rendered as part of the background, middleground or foreground */
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
	protected Prop(float x, float y, int width, int height, boolean grounded, Depth depth) {
		this.width = width;
		this.height = height;
		position = new Vector2(x, y);
		this.depth = depth;
		this.id = ParameterPersistenceService.getParameters().getNextPropId();
		this.grounded = grounded;
	}

	/** Render this {@link Prop} */
	public abstract void render();

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
		CarrotProp.carrot = new TextureRegion(Domain.gameWorldTexture, 352, 176, 12, 17);
		Anvil.anvil = new TextureRegion(Domain.gameWorldTexture, 363, 225, 44, 18);
		WorkBench.workbench = new TextureRegion(Domain.gameWorldTexture, 559, 219, 80, 33);
	}


	/** True if mouse is over this {@link Prop} */
	public boolean isMouseOver() {
		float mx = BloodAndMithrilClient.getMouseWorldX();
		float my = BloodAndMithrilClient.getMouseWorldY();

		return mx > position.x - width/2 && mx < position.x + width/2 && my > position.y && my < position.y + height;
	}


	/** Called when this {@link Prop} has been left clicked */
	public boolean leftClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}


	/** Called when this {@link Prop} has been right clicked */
	public boolean rightClick() {
		if (!isMouseOver()) {
			return false;
		}
		return true;
	}
}
