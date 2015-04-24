package bloodandmithril.item.items;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.io.Serializable;
import java.util.List;

import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
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
import bloodandmithril.performance.PositionalIndexNode;
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
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.Topography.NoTileFoundException;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * An {@link Item}
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Item implements Serializable, Affixed {
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
	private float angle, angularVelocity = (Util.getRandom().nextFloat() - 0.5f) * 40f;

	/** Whether or not this item bounces when discarded */
	private boolean bounces = false;

	/**
	 * Constructor
	 */
	protected Item(float mass, int volume, boolean equippable) {
		this.mass = mass;
		this.volume = volume;
		this.equippable = equippable;

		this.angle = rotates() ? Util.getRandom().nextFloat() * 360f : 0f;
	}

	/**
	 * Constructor
	 */
	protected Item(float mass, int volume, boolean equippable, long value) {
		this.mass = mass;
		this.volume = volume;
		this.equippable = equippable;
		this.value = value;

		this.angle = rotates() ? Util.getRandom().nextFloat() * 360f : 0f;
	}

	/** Get the singular name for this item */
	public String getSingular(boolean firstCap) {
		return modifyName(internalGetSingular(firstCap));
	}

	/** Get the plural name for this item */
	public String getPlural(boolean firstCap) {
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

	@Override
	public boolean equals(Object other) {
		if (other instanceof Item) {
			return sameAs((Item)other);
		}
		return false;
	}

	/**
	 * @return true if this {@link Item} is identical to another, including affixes
	 */
	public boolean sameAs(Item other) {
		if (minorAffixes.size() != other.minorAffixes.size()) {
			return false;
		}

		for (final Affix affix : minorAffixes) {
			Optional<MinorAffix> tryFind = Iterables.tryFind(other.minorAffixes, a -> {
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
		Item item = internalCopy();

		item.angle = angle;
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

	public void setAngularVelocity(float angVel) {
		this.angularVelocity = angVel;
	}

	/** Returns the vector from the bottom left of the texture region to the centre of rotation (location of item) */
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() / 2, rotates() ? getTextureRegion().getRegionHeight() / 2 : 0f);
	}

	/** Renders this item in world */
	public void render() {
		TextureRegion textureRegion = getTextureRegion();
		Vector2 offset = getRenderCentreOffset();

		spriteBatch.draw(
			textureRegion,
			position.x - offset.x,
			position.y - offset.y,
			offset.x,
			offset.y,
			textureRegion.getRegionWidth(),
			textureRegion.getRegionHeight(),
			1f,
			1f,
			angle
		);
	};


	public boolean isMouseOver() {
		Vector2 mouseCoords = new Vector2(
			BloodAndMithrilClient.getMouseWorldX(),
			BloodAndMithrilClient.getMouseWorldY()
		);

		TextureRegion textureRegion = getTextureRegion();
		int width = textureRegion.getRegionWidth();
		int height = textureRegion.getRegionHeight();

		// Translate coordinate system to have origin on the centre of the item (rotation pivot of rendering), then work out mouse coordinates in this coordinate system
		// Then apply rotation of -(renderAngle) to mouse coordinates in these new coordinates, result should be a rectangle. Then apply standard logic.
		mouseCoords.sub(position).add(getRenderCentreOffset().rotate(angle)).rotate(-angle);

		return mouseCoords.x > 0 && mouseCoords.x < width && mouseCoords.y > 0 && mouseCoords.y < height;
	}


	/**
	 * Loads the textures
	 */
	public static void setup() {
		Iron.IRONINGOT = new TextureRegion(WorldRenderer.gameWorldTexture, 150, 13, 22, 5);
		Steel.STEELINGOT = new TextureRegion(WorldRenderer.gameWorldTexture, 150, 19, 22, 5);
		Hematite.HEMATITE = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 131, 16, 9);
		Coal.COAL = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 118, 18, 12);
		CarrotItem.CARROT = new TextureRegion(WorldRenderer.gameWorldTexture, 352, 194, 25, 13);
		SandStone.SANDSTONE = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 141, 15, 10);
		ChickenLegItem.COOKED_CHICKEN_LEG = new TextureRegion(WorldRenderer.gameWorldTexture, 118, 1, 20, 22);
		ChickenLegItem.RAW_CHICKEN_LEG = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 1, 20, 22);
		GlassBottleItem.GLASSBOTTLE_ITEM = new TextureRegion(WorldRenderer.gameWorldTexture, 139, 1, 10, 25);
		GlassItem.GLASS_ITEM = new TextureRegion(WorldRenderer.gameWorldTexture, 150, 1, 16, 11);
		AshesItem.ASHES = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 104, 17, 6);
		SandItem.SAND = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 111, 15, 6);
		StandardWood.WOODLOG = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 67, 82, 27);
		StandardWood.WOODPLANK = new TextureRegion(WorldRenderer.gameWorldTexture, 118, 60, 40, 6);
		CurrencyItem.CURRENCY_POUCH = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 24, 18, 16);
		DeathCapItem.DEATH_CAP = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 41, 20, 25);

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

		BrickItem.BRICK = new TextureRegion(WorldRenderer.gameWorldTexture, 118, 53, 18, 6);
		DirtItem.DIRT_PILE = new TextureRegion(WorldRenderer.gameWorldTexture, 97, 95, 21, 8);

		StickItem.STICK = new TextureRegion(WorldRenderer.gameWorldTexture, 827, 132, 11, 23);
	}


	/** A window with a description of this {@link Item} */
	public Window getInfoWindow() {
		return new ItemInfoWindow(
			this,
			BloodAndMithrilClient.WIDTH/2 - 200,
			BloodAndMithrilClient.HEIGHT/2 + 175,
			400,
			350
		);
	}


	/**
	 * Processes particle effects
	 */
	protected void particleEffects() {
		// No-op default
	};


	/** Update method, delta measured in seconds */
	public void update(float delta) throws NoTileFoundException {
		if (getId() == null) {
			return;
		}

		if (!Domain.getWorld(getWorldId()).getTopography().getChunkMap().doesChunkExist(position)) {
			return;
		}

		Vector2 previousPosition = position.cpy();
		Vector2 previousVelocity = velocity.cpy();

		position.add(velocity.cpy().scl(delta));

		float gravity = Domain.getWorld(getWorldId()).getGravity();
		if (velocity.cpy().scl(delta).len() > TILE_SIZE) {
			velocity.scl(0.9f);
		}

		velocity.y = velocity.y - delta * gravity;

		Tile tileUnder = Domain.getWorld(getWorldId()).getTopography().getTile(position.x, position.y, true);
		if (rotates() && tileUnder.isPassable()) {
			angle = angle + angularVelocity;
		}

		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			Vector2 trial = position.cpy();
			trial.y += -previousVelocity.y*delta;

			if (Domain.getWorld(getWorldId()).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f) {

					int i = (int)angle % 360 - (int)getUprightAngle();
					if (i < 0) {
						i = i + 360;
					}
					boolean pointingUp = i > 350 || i > 0 && i < 190;
					if (pointingUp && bounces) {
						if (abs(velocity.y) > 400f) {
							angularVelocity = (Util.getRandom().nextFloat() - 0.5f) * 40f;
						} else {
							angularVelocity = max(angularVelocity * 0.6f, 5f);
						}
						setPosition(previousPosition);
						velocity.y = -previousVelocity.y * 0.7f;
						velocity.x = previousVelocity.x * 0.3f;
					} else {
						angularVelocity = 0f;
						velocity.x = velocity.x * 0.3f;
						velocity.y = 0f;
						position.y = Domain.getWorld(getWorldId()).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(getPosition(), true).y;
					}
				} else {
					setPosition(previousPosition);
					velocity.y = -previousVelocity.y;
				}
			} else {
				velocity.x = 0f;
				setPosition(previousPosition);
			}
		}

		updatePositionalIndex();
		particleEffects();
	}


	public void updatePositionalIndex() {
		for (PositionalIndexNode node : Domain.getWorld(worldId).getPositionalIndexMap().getNearbyNodes(position.x, position.y)) {
			node.removeItem(id);
		}

		Domain.getWorld(worldId).getPositionalIndexMap().get(position.x, position.y).addItem(id);
	}


	public ContextMenu getContextMenu() {
		MenuItem takeItem = new MenuItem(
			"Take",
			() -> {
				if (Domain.getSelectedIndividuals().size() == 1) {
					Individual selected = Domain.getSelectedIndividuals().iterator().next();
					if (ClientServerInterface.isServer()) {
						try {
							selected.getAI().setCurrentTask(new TakeItem(selected, this));
						} catch (NoTileFoundException e) {}
					} else {
						ClientServerInterface.SendRequest.sendRequestTakeItem(selected, this);
					}
				}
			},
			Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.WHITE,
			Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GREEN,
			Domain.getSelectedIndividuals().size() > 1 ? Colors.UI_DARK_GRAY : Color.GRAY,
			new ContextMenu(0, 0, true, new MenuItem(
				"You have multiple individuals selected",
				() -> {},
				Colors.UI_DARK_GRAY,
				Colors.UI_DARK_GRAY,
				Colors.UI_DARK_GRAY,
				null
			)),
			() -> {return Domain.getSelectedIndividuals().size() > 1;}
		);

		ContextMenu menu = new ContextMenu(0, 0, true,
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponentUnique(
						getInfoWindow()
					);
				},
				Color.WHITE,
				Color.GREEN,
				Color.GRAY,
				null
			)
		);

		if (!Domain.getSelectedIndividuals().isEmpty()) {
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

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void setVelocity(Vector2 velocity) {
		this.velocity = velocity;
	}

	public Integer getWorldId() {
		return worldId;
	}

	public void setWorldId(Integer worldId) {
		this.worldId = worldId;
	}

	public float getMass() {
		return mass;
	}

	public long getValue() {
		return value;
	}

	protected void setValue(long value) {
		this.value = value;
	}

	public Panel getInfoPanel() {
		ItemInfoPopupPanel panel = new ItemInfoPopupPanel(null, this);

		float width1 = Fonts.defaultFont.getBounds(getSingular(true), new TextBounds()).width;
		float width2 = Fonts.defaultFont.getBounds("Weight :" + String.format("%.2f", getMass()), new TextBounds()).width;
		float width3 = Fonts.defaultFont.getBounds("Volume :" + getVolume(), new TextBounds()).width;
		float max = Math.max(width1, Math.max(width2, width3));

		TextBounds textBounds = Fonts.defaultFont.getWrappedBounds(getDescription(), 250, new TextBounds());

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
	public void setPostAffix(PostAffix postAffix) {
		this.postAffix = postAffix;
	}

	@Override
	public void setPreAffix(PreAffix preAffix) {
		this.preAffix = preAffix;
	}

	/**
	 * @return the description of the item type
	 */
	public abstract Category getType();

	@Override
	public String modifyName(String original) {
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

	public void setVolume(int volume) {
		this.volume = volume;
	}


	public enum Category {
		ONEHANDEDSWORD("One-handed sword"),
		DAGGER("Dagger"),
		ONEHANDEDAXE("One-handed axe"),
		ONEHANDEDBLUNT("One-handed Blunt"),
		ONEHANDEDSPEAR("One-handed spear"),
		BOW("Bow"),
		RING("Ring"),
		PICKAXE("Pickaxe"),

		EARTH("Earth"),
		MATERIAL("Material"),

		FOOD("Food"),
		CONTAINER("Container"),

		AMMO("Ammunition"),
		KEY("Key"),
		MISC("Miscellaneous"),
		OFFHAND("Off-hand"),
		SEED("Seed"),
		FURNITURE("Furniture");

		private String value;
		private Category(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}


	public Box getPickupBox() {
		return new Box(position.cpy(), 75f, 75f);
	}


	/**
	 * @return The angle from positive axis of the upright position of the texture region, in degrees, measured positive counter clockwise
	 */
	public abstract float getUprightAngle();
}