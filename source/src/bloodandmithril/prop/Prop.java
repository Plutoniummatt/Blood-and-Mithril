package bloodandmithril.prop;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;

import java.io.Serializable;
import java.util.Collection;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.MouseOverable;
import bloodandmithril.core.UpdatedBy;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.RenderPropWith;
import bloodandmithril.graphics.Textures;
import bloodandmithril.graphics.WorldRenderer.Depth;
import bloodandmithril.item.items.food.plant.CarrotItem.CarrotSeedProp;
import bloodandmithril.performance.PositionalIndexChunkNode;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.MedievalWallTorchProp;
import bloodandmithril.prop.furniture.RottenWoodenChest;
import bloodandmithril.prop.furniture.SmallWoodenCrateProp;
import bloodandmithril.prop.furniture.WoodenChestProp;
import bloodandmithril.prop.plant.CarrotProp;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.SerializableFunction;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

/**
 * A renderable {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
@UpdatedBy()
@RenderPropWith()
public abstract class Prop implements Serializable, Visible, MouseOverable {
	private static final long serialVersionUID = -1659783923740689585L;

	/** Dimensions of this {@link Prop} */
	public int width, height;

	/** Whether this prop will be rendered as part of the background, middleground or foreground */
	public final Depth depth;

	/** id of this prop */
	public int id;

	/** The location of this {@link Prop} */
	public Vector2 position;

	/** True if this {@link Prop} must be placed on the ground */
	public final boolean grounded;

	/** True if this prop prevents tiles underneath from being destroyed */
	public final boolean preventsMining;

	private int worldId;

	/** Returns whether this {@link Construction} can be placed on a tile type */
	private final SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf;
	private SerializableMappingFunction<Tile, Boolean> canPlaceInFrontOf;

	/**
	 * Constructor
	 */
	protected Prop(final float x, final float y, final int width, final int height, final boolean grounded, final Depth depth, final SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf, final boolean preventsMining) {
		this.width = width;
		this.height = height;
		this.canPlaceOnTopOf = canPlaceOnTopOf;
		this.preventsMining = preventsMining;
		this.position = new Vector2(x, y);
		this.depth = depth;
		this.id = Wiring.injector().getInstance(ParameterPersistenceService.class).getParameters().getNextPropId();
		this.grounded = grounded;
	}

	/** Synchronizes this prop with another */
	public abstract void synchronizeProp(Prop other);

	/** Get the right-click {@link ContextMenu} */
	public abstract ContextMenu getContextMenu();

	/** Whether this prop can be used as a source of fire */
	public abstract boolean canBeUsedAsFireSource();

	/** Return the color of the context menu button for this {@link Prop} */
	public abstract Color getContextMenuColor();

	/** Returns the string title of this {@link Prop} */
	public String getTitle() {
		return getContextMenuItemLabel();
	}

	/** Reindexes this item */
	public void updatePositionIndex() {
		for (final PositionalIndexChunkNode node : Domain.getWorld(worldId).getPositionalIndexChunkMap().getNearbyNodes(position.x, position.y)) {
			node.removeProp(id);
		}

		Domain.getWorld(worldId).getPositionalIndexChunkMap().get(position.x, position.y).addProp(id);
	}

	/** Returns the label to use for the right click context menu */
	public abstract String getContextMenuItemLabel();

	public static void setup() {
		Furnace.FURNACE = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 286, 95, 56);
		Furnace.FURNACE_BURNING = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 1, 343, 95, 56);

		Campfire.CAMPFIRE = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 554, 176, 64, 32);
		WoodenChestProp.WOODEN_CHEST = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 396, 144, 56, 31);
		RottenWoodenChest.ROTTEN_WOODEN_CHEST = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 396, 112, 56, 31);
		SmallWoodenCrateProp.WOODEN_CRATE = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 453, 140, 44, 35);
		CarrotProp.CARROT = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 352, 173, 12, 20);
		BlacksmithWorkshop.BLACKSMITH_WORKSHOP_WORKING = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 591, 132, 117, 43);
		BlacksmithWorkshop.BLACKSMITH_WORKSHOP = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 709, 132, 117, 43);
		WorkBench.WORKBENCH = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 499, 132, 90, 43);
		CarrotSeedProp.CARROT_SEED = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 389, 177, 16, 16);
		CarrotProp.HALF_CARROT = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 406, 177, 16, 16);
		MedievalWallTorchProp.MEDIEVAL_WALL_TORCH = new TextureRegion(Textures.GAME_WORLD_TEXTURE, 678, 225, 13, 30);
	}


	@Override
	public String getMenuTitle() {
		return getTitle();
	}


	/** True if mouse is over this {@link Prop} */
	@Override
	public boolean isMouseOver() {
		final float mx = getMouseWorldX();
		final float my = getMouseWorldY();

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

	public int getWorldId() {
		return worldId;
	}

	public void setWorldId(final int worldId) {
		this.worldId = worldId;
	}


	public SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf() {
		return canPlaceOnTopOf;
	}


	public SerializableMappingFunction<Tile, Boolean> canPlaceInFrontOf() {
		return canPlaceInFrontOf;
	}


	protected void canPlaceInFrontOf(final SerializableMappingFunction<Tile, Boolean> function) {
		this.canPlaceInFrontOf = function;
	}


	@Override
	public Collection<Vector2> getVisibleLocations() {
		return Lists.newArrayList(
			position.cpy(),
			position.cpy().add(-width/2, 0),
			position.cpy().add(width/2, 0),
			position.cpy().add(-width/2, height),
			position.cpy().add(width/2, height)
		);
	}


	@Override
	public boolean isVisible() {
		return true;
	}


	public Box getBoundingBox() {
		return new Box(position.cpy().add(0f, height/2f), width, height);
	}


	@Override
	public boolean sameAs(final Visible other) {
		if (other instanceof Prop) {
			return ((Prop) other).id == id;
		}

		return false;
	}


	public static class ReturnPropPosition implements SerializableFunction<Vector2> {
		private static final long serialVersionUID = 6231253952557168072L;
		private Prop prop;
		public ReturnPropPosition(final Prop prop) {
			this.prop = prop;
		}

		@Override
		public Vector2 call() {
			return prop.position;
		}
	}
}
