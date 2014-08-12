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
import bloodandmithril.item.affix.Affix;
import bloodandmithril.item.affix.Affixed;
import bloodandmithril.item.affix.MinorAffix;
import bloodandmithril.item.affix.PostAffix;
import bloodandmithril.item.affix.PreAffix;
import bloodandmithril.item.items.food.plant.Carrot;
import bloodandmithril.item.material.metal.Copper;
import bloodandmithril.item.material.metal.Gold;
import bloodandmithril.item.material.metal.Iron;
import bloodandmithril.item.material.metal.Silver;
import bloodandmithril.item.material.metal.Steel;
import bloodandmithril.item.material.mineral.Coal;
import bloodandmithril.item.material.mineral.Hematite;
import bloodandmithril.networking.ClientServerInterface;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.ItemInfoWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
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

	/**
	 * Constructor
	 */
	protected Item(float mass, boolean equippable) {
		this.mass = mass;
		this.equippable = equippable;

		this.angle = rotates() ? Util.getRandom().nextFloat() * 360f : 0f;
	}

	/**
	 * Constructor
	 */
	protected Item(float mass, boolean equippable, long value) {
		this.mass = mass;
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

	/** Get the singular name for this item */
	protected abstract String internalGetSingular(boolean firstCap);

	/** Get the plural name for this item */
	protected abstract String internalGetPlural(boolean firstCap);

	/** Returns the string description of this {@link Item} */
	public abstract String getDescription();

	/** Returns true if two {@link Item}s have identical attributes */
	protected abstract boolean internalSameAs(Item other);

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
	protected abstract TextureRegion getTextureRegion();

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

		return item;
	}

	protected abstract Item internalCopy();

	/** Whether to call standard {@link #update(float)} or {@link #updateRigid(float)} */
	public boolean rotates() {
		return false;
	}

	public void setAngularVelocity(float angVel) {
		this.angularVelocity = angVel;
	}

	/** Returns the vector from the bottom left of the texture region to the centre of rotation (location of item) */
	protected Vector2 getRenderCentreOffset() {
		return new Vector2(getTextureRegion().getRegionWidth() / 2, 0f);
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
		Iron.IRONINGOT = new TextureRegion(Domain.gameWorldTexture, 372, 246, 18, 6);
		Steel.STEELINGOT = new TextureRegion(Domain.gameWorldTexture, 392, 246, 18, 6);
		Hematite.HEMATITE = new TextureRegion(Domain.gameWorldTexture, 372, 253, 18, 11);
		Coal.COAL = new TextureRegion(Domain.gameWorldTexture, 372, 265, 18, 11);
		Carrot.CARROT = new TextureRegion(Domain.gameWorldTexture, 365, 180, 23, 13);

		Silver.SILVERINGOTICON = new TextureRegion(UserInterface.iconTexture, 0, 0, 64, 64);
		Iron.IRONINGOTICON = new TextureRegion(UserInterface.iconTexture, 65, 0, 64, 64);
		Steel.STEELINGOTICON = new TextureRegion(UserInterface.iconTexture, 130, 0, 64, 64);
		Copper.COPPERINGOTICON = new TextureRegion(UserInterface.iconTexture, 195, 0, 64, 64);
		Gold.GOLDINGOTICON = new TextureRegion(UserInterface.iconTexture, 260, 0, 64, 64);
	}


	/** A window with a description of this {@link Item} */
	public Window getInfoWindow() {
		return new ItemInfoWindow(
			this,
			BloodAndMithrilClient.WIDTH/2 - 200,
			BloodAndMithrilClient.HEIGHT/2 + 250,
			400,
			450
		);
	}


	/** Update method, delta measured in seconds */
	public void update(float delta) {
		if (getId() == null) {
			return;
		}

		Vector2 previousPosition = position.cpy();
		Vector2 previousVelocity = velocity.cpy();

		position.add(velocity.cpy().mul(delta));

		float gravity = Domain.getWorld(getWorldId()).getGravity();
		if (abs((velocity.y - gravity * delta) * delta) < TILE_SIZE/2) {
			velocity.y = velocity.y - delta * gravity;
		} else {
			velocity.y = velocity.y * 0.8f;
		}

		Tile tileUnder = Domain.getWorld(getWorldId()).getTopography().getTile(position.x, position.y, true);
		if (rotates() && tileUnder.isPassable()) {
			angle = angle + angularVelocity;
		}

		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			Vector2 trial = position.cpy();
			trial.y += -previousVelocity.y*delta;

			if (Domain.getWorld(getWorldId()).getTopography().getTile(trial.x, trial.y, true).isPassable()) {
				if (previousVelocity.y <= 0f) {

					int i = (int)angle % 360;
					if (i < 0) {
						i = i + 360;
					}
					boolean pointingUp = i > 350 || i > 0 && i < 190;
					if (pointingUp) {
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
	}


	public ContextMenu getContextMenu() {
		MenuItem takeItem = new MenuItem(
			"Take",
			() -> {
				if (Domain.getSelectedIndividuals().size() == 1) {
					Individual selected = Domain.getSelectedIndividuals().iterator().next();
					if (ClientServerInterface.isServer()) {
						selected.getAI().setCurrentTask(new TakeItem(selected, this));
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
	public abstract String getType();

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
}