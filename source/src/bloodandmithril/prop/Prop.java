package bloodandmithril.prop;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;

import java.io.Serializable;
import java.util.Collection;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.food.plant.Carrot.CarrotSeedProp;
import bloodandmithril.performance.PositionalIndexNode;
import bloodandmithril.persistence.ParameterPersistenceService;
import bloodandmithril.prop.construction.Construction;
import bloodandmithril.prop.construction.craftingstation.BlacksmithWorkshop;
import bloodandmithril.prop.construction.craftingstation.Campfire;
import bloodandmithril.prop.construction.craftingstation.Furnace;
import bloodandmithril.prop.construction.craftingstation.WorkBench;
import bloodandmithril.prop.furniture.MedievalWallTorch;
import bloodandmithril.prop.furniture.SmallWoodenCrate;
import bloodandmithril.prop.furniture.WoodenChest;
import bloodandmithril.prop.plant.CarrotProp;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.util.Function;
import bloodandmithril.util.SerializableMappingFunction;
import bloodandmithril.world.Domain;
import bloodandmithril.world.Domain.Depth;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.collect.Lists;

/**
 * A renderable {@link Prop}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Prop implements Serializable, Visible {
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
	public final boolean grounded;

	private int worldId;

	/** Returns whether this {@link Construction} can be placed on a tile type */
	private final SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf;
	private SerializableMappingFunction<Tile, Boolean> canPlaceInFrontOf;

	/**
	 * Constructor
	 */
	protected Prop(float x, float y, int width, int height, boolean grounded, Depth depth, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf) {
		this.width = width;
		this.height = height;
		this.canPlaceOnTopOf = canPlaceOnTopOf;
		this.position = new Vector2(x, y);
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

	/** Whether this prop can be used as a source of fire */
	public abstract boolean canBeUsedAsFireSource();

	/** Returns the string title of this {@link Prop} */
	public String getTitle() {
		return getContextMenuItemLabel();
	}

	/** Reindexes this item */
	public void updatePositionIndex() {
		for (PositionalIndexNode node : Domain.getWorld(worldId).getPositionalIndexMap().getNearbyNodes(position.x, position.y)) {
			node.removeProp(id);
		}

		Domain.getWorld(worldId).getPositionalIndexMap().get(position.x, position.y).addProp(id);
	}

	/** Returns the label to use for the right click context menu */
	public abstract String getContextMenuItemLabel();

	public static void setup() {
		Campfire.CAMPFIRE = new TextureRegion(Domain.gameWorldTexture, 554, 176, 64, 32);
		Furnace.FURNACE_BURNING = new TextureRegion(Domain.gameWorldTexture, 453, 176, 95, 56);
		Furnace.FURNACE = new TextureRegion(Domain.gameWorldTexture, 453, 233, 95, 56);
		WoodenChest.woodenChest = new TextureRegion(Domain.gameWorldTexture, 408, 206, 44, 35);
		SmallWoodenCrate.woodenCrate = new TextureRegion(Domain.gameWorldTexture, 453, 140, 44, 35);
		CarrotProp.carrot = new TextureRegion(Domain.gameWorldTexture, 352, 173, 12, 20);
		BlacksmithWorkshop.blackSmithWorkshopWorking = new TextureRegion(Domain.gameWorldTexture, 591, 114, 117, 61);
		BlacksmithWorkshop.blackSmithWorkshop = new TextureRegion(Domain.gameWorldTexture, 709, 114, 117, 61);
		WorkBench.workbench = new TextureRegion(Domain.gameWorldTexture, 499, 132, 90, 43);
		CarrotSeedProp.carrotSeed = new TextureRegion(Domain.gameWorldTexture, 389, 177, 16, 16);
		CarrotProp.halfCarrot = new TextureRegion(Domain.gameWorldTexture, 406, 177, 16, 16);
		MedievalWallTorch.medievalWallTorch = new TextureRegion(Domain.gameWorldTexture, 678, 225, 13, 30);
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

	public int getWorldId() {
		return worldId;
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}

	/**
	 * @return whether this prop can be placed at this props location
	 */
	public boolean canPlaceAtCurrentPosition() {
		return canPlaceAt(position);
	}


	/**
	 * @return whether this prop can be placed at a given location
	 */
	public boolean canPlaceAt(Vector2 position) {
		return canPlaceAt(position.x, position.y, width, height, canPlaceOnTopOf, canPlaceInFrontOf, grounded, () -> {
			for (Integer propId : Domain.getActiveWorld().getPositionalIndexMap().getNearbyEntities(Prop.class, position.x, position.y)) {
				Prop prop = Domain.getActiveWorld().props().getProp(propId);
				if (Domain.getActiveWorld().props().hasProp(propId)) {
					if (this.overlapsWith(prop)) {
						return false;
					}
				}
			}
			
			return true;
		});
	}


	/**
	 * @return whether this prop can be placed at a given location
	 */
	public static boolean canPlaceAt(float x, float y, float width, float height, SerializableMappingFunction<Tile, Boolean> canPlaceOnTopOf, SerializableMappingFunction<Tile, Boolean> canPlaceInFrontOf, boolean grounded, Function<Boolean> customFunction) {
		float xStep = width / TILE_SIZE;
		long xSteps = Math.round(Math.ceil(xStep));
		float xIncrement = width / xSteps;

		float yStep = height / TILE_SIZE;
		long ySteps = Math.round(Math.ceil(yStep));
		float yIncrement = height / ySteps;


		try {
			for (int i = 0; i <= xSteps; i++) {
				Tile tileUnder = Domain.getActiveWorld().getTopography().getTile(x - width / 2 + i * xIncrement, y - TILE_SIZE/2, true);
				if (grounded && (tileUnder.isPassable() || canPlaceOnTopOf != null && !canPlaceOnTopOf.apply(tileUnder))) {
					return false;
				}

				for (int j = 1; j <= ySteps; j++) {
					Tile tileOverlapping = Domain.getActiveWorld().getTopography().getTile(x - width / 2 + i * xIncrement, y + j * yIncrement - TILE_SIZE/2, true);
					Tile tileUnderlapping = Domain.getActiveWorld().getTopography().getTile(x - width / 2 + i * xIncrement, y + j * yIncrement - TILE_SIZE/2, false);
					if (!tileOverlapping.isPassable() || canPlaceInFrontOf != null && !canPlaceInFrontOf.apply(tileUnderlapping)) {
						return false;
					}
				}
			}
		} catch (NoTileFoundException e) {
			return false;
		}

		return customFunction.call();
	}


	protected void canPlaceInFrontOf(SerializableMappingFunction<Tile, Boolean> function) {
		this.canPlaceInFrontOf = function;
	}


	private boolean overlapsWith(Prop other) {
		float left = position.x - width/2;
		float right = position.x + width/2;
		float top = position.y + height;
		float bottom = position.y;

		float otherLeft = other.position.x - other.width/2;
		float otherRight = other.position.x + other.width/2;
		float otherTop = other.position.y + other.height;
		float otherBottom = other.position.y;

		return
			!(left >= otherRight) &&
			!(right <= otherLeft) &&
			!(top <= otherBottom) &&
			!(bottom >= otherTop);
	}

	public abstract void preRender();
	

	@Override
	public Collection<Vector2> getVisibleLocations() {
		return Lists.newArrayList(position.cpy());
	}
	
	
	@Override
	public boolean isVisible() {
		return true;
	}
}
