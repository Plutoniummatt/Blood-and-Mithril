package bloodandmithril.item.items;

import static bloodandmithril.control.InputUtilities.getMouseWorldX;
import static bloodandmithril.control.InputUtilities.getMouseWorldY;
import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import bloodandmithril.character.ai.perception.Visible;
import bloodandmithril.character.ai.routine.EntityVisibleRoutine.EntityVisible;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.GameClientStateTracker;
import bloodandmithril.core.MouseOverable;
import bloodandmithril.core.Wiring;
import bloodandmithril.graphics.Graphics;
import bloodandmithril.graphics.WorldRenderer;
import bloodandmithril.item.affix.Affix;
import bloodandmithril.item.affix.Affixed;
import bloodandmithril.item.affix.MinorAffix;
import bloodandmithril.item.affix.PostAffix;
import bloodandmithril.item.affix.PreAffix;
import bloodandmithril.item.items.container.GlassBottleItem;
import bloodandmithril.item.items.equipment.weapon.onehandedsword.Broadsword;
import bloodandmithril.item.items.food.animal.ChickenLegItem;
import bloodandmithril.item.items.food.plant.CarrotItem;
import bloodandmithril.item.items.food.plant.DeathCapItem;
import bloodandmithril.item.items.material.BrickItem;
import bloodandmithril.item.items.material.GlassItem;
import bloodandmithril.item.items.material.LogItem;
import bloodandmithril.item.items.material.PlankItem;
import bloodandmithril.item.items.material.StickItem;
import bloodandmithril.item.items.mineral.earth.AshesItem;
import bloodandmithril.item.items.mineral.earth.DirtItem;
import bloodandmithril.item.items.mineral.earth.SandItem;
import bloodandmithril.item.items.misc.CurrencyItem;
import bloodandmithril.item.material.metal.Copper;
import bloodandmithril.item.material.metal.Gold;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Silver;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.Hematite;
import bloodandmithril.item.material.mineral.SandStone;
import bloodandmithril.item.material.wood.StandardWood;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.Panel;
import bloodandmithril.ui.components.panel.ItemInfoPopupPanel;
import bloodandmithril.ui.components.window.ItemInfoWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Fonts;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.util.datastructure.Box;
import bloodandmithril.util.datastructure.WrapperForTwo;
import bloodandmithril.world.topography.Topography.NoTileFoundException;

/**
 * An {@link Item}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Item implements Serializable, Affixed, MouseOverable, Visible {
	private static final long serialVersionUID = -7733840667288631158L;

	/** Affixes of this {@link Item} */
	protected List<MinorAffix> minorAffixes = newArrayList();

	/** {@link PreAffix} of this {@link Item} */
	protected PreAffix preAffix;

	/** {@link PostAffix} of this {@link Item} */
	protected PostAffix postAffix;

	/** The mass of this item */
	private float mass;

	/** The volume of this item */
	private int volume;

	/** The value of this item */
	private long value;

	/** Whether this item can be equipped by an {@link Individual} */
	private boolean equippable;

	/** The position and velocity of this item, if it exists on the world */
	private Vector2 position, velocity;

	/** ID of this item, it should be null when stored inside a container, this is a dynamically changing field, it is not an immutable */
	private Integer id, worldId;

	/** Rotating when in world */
	private float angle;
	private float angularVelocity = (Util.getRandom().nextFloat() - 0.5f) * 40f;

	/** Whether or not this item bounces when discarded */
	private boolean bounces = false;

	/**
	 * Constructor
	 */
	protected Item(final float mass, final int volume, final boolean equippable) {
		this.mass = mass;
		this.volume = volume;
		this.equippable = equippable;

		this.setAngle(rotates() ? Util.getRandom().nextFloat() * 360f : 0f);
	}

	/**
	 * Constructor
	 */
	protected Item(final float mass, final int volume, final boolean equippable, final long value) {
		this.mass = mass;
		this.volume = volume;
		this.equippable = equippable;
		this.value = value;

		this.setAngle(rotates() ? Util.getRandom().nextFloat() * 360f : 0f);
	}

	/** Get the singular name for this item */
	public String getSingular(final boolean firstCap) {
		return modifyName(internalGetSingular(firstCap));
	}

	/** Get the plural name for this item */
	public String getPlural(final boolean firstCap) {
		return modifyName(internalGetPlural(firstCap));
	}

	/** Whether this item can be thrown */
	public boolean throwable() {
		return true;
	}

	/** Get the singular name for this item */
	protected abstract String internalGetSingular(boolean firstCap);

	/** Get the plural name for this item */
	protected abstract String internalGetPlural(boolean firstCap);

	/** Returns the string description of this {@link Item} */
	public abstract String getDescription();

	/** Returns true if two {@link Item}s have identical attributes */
	protected abstract boolean internalSameAs(Item other);

	/** Set bouncing to true */
	public Item bounces() {
		this.bounces = true;
		return this;
	}

	/** Return true if this item bounces */
	public boolean doesBounce() {
		return bounces;
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof Item) {
			return sameAs((Item)other);
		}
		return false;
	}

	/**
	 * @return true if this {@link Item} is identical to another, including affixes
	 */
	public boolean sameAs(final Item other) {
		if (minorAffixes.size() != other.minorAffixes.size()) {
			return false;
		}

		for (final Affix affix : minorAffixes) {
			final Optional<MinorAffix> tryFind = Iterables.tryFind(other.minorAffixes, a -> {
				return a.getClass().equals(affix.getClass());
			});

			if (tryFind.isPresent()) {
				if (tryFind.get().isSameAs(affix)) {
					continue;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		if (preAffix == null) {
			if (other.getPreAffix() != null) {
				return false;
			}
		} else {
			if (other.getPreAffix() != null) {
				if (!preAffix.isSameAs(other.getPreAffix())) {
					return false;
				}
			} else {
				return false;
			}
		}

		if (postAffix == null) {
			if (other.getPostAffix() != null) {
				return false;
			}
		} else {
			if (other.getPostAffix() != null) {
				if (!postAffix.isSameAs(other.getPostAffix())) {
					return false;
				}
			} else {
				return false;
			}
		}

		return internalSameAs(other);
	}

	/** Gets the {@link TextureRegion} of this {@link Item} */
	public abstract TextureRegion getTextureRegion();

	/** Gets the {@link TextureRegion} for the Icon of this {@link Item} */
	public abstract TextureRegion getIconTextureRegion();

	/** Clones this {@link Item}, WARNING : ID IS NOT CLONED! */
	public Item copy() {
		final Item item = internalCopy();

		item.setAngle(angle);
		item.angularVelocity = angularVelocity;
		item.equippable = equippable;
		item.mass = mass;
		item.setValue(value);
		item.minorAffixes = newArrayList(minorAffixes);
		item.preAffix = preAffix;
		item.postAffix = postAffix;

		return item;
	}

	protected abstract Item internalCopy();

	/** Whether to call standard {@link #update(float)} or {@link #updateRigid(float)} */
	public boolean rotates() {
		return true;
	}

	public void setAngularVelocity(final float angVel) {
		this.angularVelocity = angVel;
	}

	/** Returns the vector from the bottom left of the texture region to the centre of rotation (location of item) */
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() / 2, rotates() ? getTextureRegion().getRegionHeight() / 2 : 0f);
	}

	/** Renders this item in world */
	public void render(final Graphics graphics) {
		final TextureRegion textureRegion = getTextureRegion();
		final Vector2 offset = getRenderCentreOffset();

		graphics.getSpriteBatch().draw(
			textureRegion,
			position.x - offset.x,
			position.y - offset.y,
			offset.x,
			offset.y,
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			1f,
			1f,
			getAngle()
		);
	};


	@Override
	public String getMenuTitle() {
		return getSingular(true);
	}


	@Override
	public boolean isMouseOver() {
		final Vector2 mouseCoords = new Vector2(
			getMouseWorldX(),
			getMouseWorldY()
		);

		final TextureRegion textureRegion = getTextureRegion();
		final int width = textureRegion.getRegionWidth();
		final int height = textureRegion.getRegionHeight();

		// Translate coordinate system to have origin on the centre of the item (rotation pivot of rendering), then work out mouse coordinates in this coordinate system
		// Then apply rotation of -(renderAngle) to mouse coordinates in these new coordinates, result should be a rectangle. Then apply standard logic.
		mouseCoords.sub(position).add(getRenderCentreOffset().rotate(getAngle())).rotate(-getAngle());

		return mouseCoords.x > 0 && mouseCoords.x < width && mouseCoords.y > 0 && mouseCoords.y < height;
	}


	/**
	 * Loads the textures
	 */
	public static void setup() {
		Iron.IRONINGOT = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 150, 13, 22, 5);
		Steel.STEELINGOT = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 150, 19, 22, 5);
		Hematite.HEMATITE = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 131, 16, 9);
		Coal.COAL = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 118, 18, 12);
		CarrotItem.CARROT = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 352, 194, 25, 13);
		SandStone.SANDSTONE = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 141, 15, 10);
		ChickenLegItem.COOKED_CHICKEN_LEG = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 118, 1, 20, 22);
		ChickenLegItem.RAW_CHICKEN_LEG = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 1, 20, 22);
		GlassBottleItem.GLASSBOTTLE_ITEM = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 139, 1, 10, 25);
		GlassItem.GLASS_ITEM = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 150, 1, 16, 11);
		AshesItem.ASHES = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 104, 17, 6);
		SandItem.SAND = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 111, 15, 6);
		StandardWood.WOODLOG = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 67, 82, 27);
		StandardWood.WOODPLANK = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 118, 60, 40, 6);
		CurrencyItem.CURRENCY_POUCH = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 24, 18, 16);
		DeathCapItem.DEATH_CAP = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 41, 20, 25);

		Silver.SILVERINGOTICON = new TextureRegion(UserInterface.iconTexture, 0, 0, 64, 64);
		Iron.IRONINGOTICON = new TextureRegion(UserInterface.iconTexture, 65, 0, 64, 64);
		Steel.STEELINGOTICON = new TextureRegion(UserInterface.iconTexture, 130, 0, 64, 64);
		Copper.COPPERINGOTICON = new TextureRegion(UserInterface.iconTexture, 195, 0, 64, 64);
		Gold.GOLDINGOTICON = new TextureRegion(UserInterface.iconTexture, 260, 0, 64, 64);
		Broadsword.ICON = new TextureRegion(UserInterface.iconTexture, 325, 0, 64, 64);
		PlankItem.PLANKICON = new TextureRegion(UserInterface.iconTexture, 390, 0, 64, 64);
		ChickenLegItem.COOKED_CHICKEN_LEG_ICON = new TextureRegion(UserInterface.iconTexture, 455, 0, 64, 64);
		ChickenLegItem.RAW_CHICKEN_LEG_ICON = new TextureRegion(UserInterface.iconTexture, 520, 0, 64, 64);
		LogItem.ICON = new TextureRegion(UserInterface.iconTexture, 585, 0, 64, 64);

		BrickItem.BRICK = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 118, 53, 18, 6);
		DirtItem.DIRT_PILE = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 97, 95, 21, 8);

		StickItem.STICK = new TextureRegion(WorldRenderer.GAME_WORLD_TEXTURE, 827, 132, 11, 23);
	}


	/** A window with a description of this {@link Item} */
	public Window getInfoWindow() {
		return new ItemInfoWindow(
			this,
			400,
			350
		);
	}


	public ContextMenu getContextMenu() {
		final GameClientStateTracker gameClientStateTracker = Wiring.injector().getInstance(GameClientStateTracker.class);

		final MenuItem takeItem = new MenuItem(
			"Take",
			() -> {
				if (gameClientStateTracker.getSelectedIndividuals().size() == 1) {
					final Individual selected = gameClientStateTracker.getSelectedIndividuals().iterator().next();
					if (ClientServerInterface.isServer()) {
						try {
							selected.getAI().setCurrentTask(new TakeItem(selected, this));
						} catch (final NoTileFoundException e) {}
					} else {
						ClientServerInterface.SendRequest.sendRequestTakeItem(selected, this);
					}
				}
			},
			gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
			gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
			gameClientStateTracker.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
			() -> { return new ContextMenu(0, 0, true, new MenuItem(
				"You have multiple individuals selected",
				() -> {},
				Colors.UI_DARK_GRAY,
				Colors.UI_DARK_GRAY,
				Colors.UI_DARK_GRAY,
				null
			));},
			() -> {return gameClientStateTracker.getSelectedIndividuals().size() > 1;}
		);

		final ContextMenu menu = new ContextMenu(0, 0, true,
			new MenuItem(
				"Show info",
				() -> {
					Wiring.injector().getInstance(UserInterface.class).addLayeredComponentUnique(
						getInfoWindow()
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (!gameClientStateTracker.getSelectedIndividuals().isEmpty()) {
			menu.addMenuItem(takeItem);
		}

		return menu;
	}


	@Override
	public String toString() {
		return internalGetSingular(true);
	}

	public Vector2 getPosition() {
		return position;
	}

	public void setPosition(final Vector2 position) {
		this.position = position;
	}

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(final Vector2 velocity) {
		this.velocity = velocity;
	}

	public Integer getWorldId() {
		return worldId;
	}

	public void setWorldId(final Integer worldId) {
		this.worldId = worldId;
	}

	public float getMass() {
		return mass;
	}

	public long getValue() {
		return value;
	}

	protected void setValue(final long value) {
		this.value = value;
	}

	public Panel getInfoPanel() {
		final ItemInfoPopupPanel panel = new ItemInfoPopupPanel(null, this);

		final float width1 = Fonts.defaultFont.getBounds(getSingular(true), new TextBounds()).width;
		final float width2 = Fonts.defaultFont.getBounds("Weight :" + String.format("%.2f", getMass()), new TextBounds()).width;
		final float width3 = Fonts.defaultFont.getBounds("Volume :" + getVolume(), new TextBounds()).width;
		final float max = Math.max(width1, Math.max(width2, width3));

		final TextBounds textBounds = Fonts.defaultFont.getWrappedBounds(getDescription(), 250, new TextBounds());

		panel.width = (int) Math.max(textBounds.width, max + 150);
		panel.height = (int) textBounds.height + 94;
		return panel;
	}

	public boolean isEquippable() {
		return equippable;
	}

	@Override
	public List<MinorAffix> getMinorAffixes() {
		return minorAffixes;
	}

	@Override
	public PostAffix getPostAffix() {
		return postAffix;
	}

	@Override
	public PreAffix getPreAffix() {
		return preAffix;
	}

	@Override
	public void setPostAffix(final PostAffix postAffix) {
		this.postAffix = postAffix;
	}

	@Override
	public void setPreAffix(final PreAffix preAffix) {
		this.preAffix = preAffix;
	}

	/**
	 * @return the description of the item type
	 */
	public abstract ItemCategory getType();

	@Override
	public String modifyName(final String original) {
		String toReturn = original;
		if (preAffix != null) {
			toReturn = preAffix.modifyName(toReturn);
		}
		if (postAffix != null) {
			toReturn = postAffix.modifyName(toReturn);
		}

		return toReturn;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(final int volume) {
		this.volume = volume;
	}


	public enum ItemCategory {
		ONEHANDEDSWORD("One-handed sword", Color.ORANGE),
		DAGGER("Dagger", Color.ORANGE),
		ONEHANDEDAXE("One-handed axe", Color.ORANGE),
		ONEHANDEDBLUNT("One-handed Blunt", Color.ORANGE),
		ONEHANDEDSPEAR("One-handed spear", Color.ORANGE),
		BOW("Bow", Color.ORANGE),
		RING("Ring", Color.ORANGE),
		PICKAXE("Pickaxe", Color.ORANGE),

		EARTH("Earth", Color.WHITE),
		MATERIAL("Material", Color.WHITE),

		FOOD("Food", Color.GREEN),
		CONTAINER("Container", Color.MAGENTA),

		AMMO("Ammunition", Color.ORANGE),
		KEY("Key", Color.WHITE),
		MISC("Miscellaneous", Color.WHITE),
		OFFHAND("Off-hand", Color.ORANGE),
		SEED("Seed", Color.WHITE),
		FURNITURE("Furniture", Color.MAROON);

		private String value;
		private Color color;
		private ItemCategory(final String value, final Color color) {
			this.value = value;
			this.color = color;
		}

		public String getValue() {
			return value;
		}

		public Color getColor() {
			return color;
		}
	}


	public Box getPickupBox() {
		return new Box(position.cpy(), 75f, 75f);
	}


	/**
	 * @return The angle from positive axis of the upright position of the texture region, in degrees, measured positive counter clockwise
	 */
	public abstract float getUprightAngle();


	@Override
	public boolean isVisible() {
		return true;
	}


	public Box getBoundingBox() {
		return new Box(position.cpy().add(0f, getTextureRegion().getRegionHeight()/2f), getTextureRegion().getRegionWidth(), getTextureRegion().getRegionHeight());
	}


	@Override
	public boolean sameAs(final Visible other) {
		if (other instanceof Item) {
			return ((Item) other).id == id;
		}

		return false;
	}


	@Override
	public Collection<Vector2> getVisibleLocations() {
		final LinkedList<Vector2> locations = Lists.newLinkedList();
		for (int i = 2; i < getTextureRegion().getRegionHeight() - 2 ; i += 2) {
			locations.add(position.cpy().add(0f, i));
		}
		return locations;
	}


	public float getAngle() {
		return angle;
	}


	public void setAngle(final float angle) {
		this.angle = angle;
	}


	public float getAngularVelocity() {
		return angularVelocity;
	}


	public static class VisibleItem extends EntityVisible {
		private static final long serialVersionUID = -211448711527852658L;
		private WrapperForTwo<Class<? extends Item>, Item> wrapper = WrapperForTwo.wrap(Item.class, null);

		@Override
		public Boolean apply(final Visible input) {
			if (input instanceof Item) {
				return true;
			}

			return false;
		}

		@Override
		public String getDetailedDescription(final Individual host) {
			return "This routine occurs when an item is visible to " + host.getId().getSimpleName();
		}

		@Override
		@SuppressWarnings("unchecked")
		public WrapperForTwo<Class<? extends Item>, Item> getEntity() {
			return wrapper;
		}
	}
}