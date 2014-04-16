package bloodandmithril.item;

import static bloodandmithril.core.BloodAndMithrilClient.spriteBatch;
import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.io.Serializable;

import bloodandmithril.character.Individual;
import bloodandmithril.character.ai.task.TakeItem;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.csi.ClientServerInterface;
import bloodandmithril.item.material.metal.IronIngot;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.ContextMenu;
import bloodandmithril.ui.components.ContextMenu.MenuItem;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.util.Util;
import bloodandmithril.util.Util.Colors;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * An {@link Item}
 *
 * @author Matt
 */
public abstract class Item implements Serializable {
	private static final long serialVersionUID = -7733840667288631158L;

	/** The mass of this item */
	public final float mass;

	/** The value of this item */
	public final long value;

	/** Whether this item can be equipped by an {@link Individual} */
	public final boolean equippable;

	/** The position and velocity of this item, if it exists on the world */
	private Vector2 position, velocity;

	/** ID of this item, it should be null when stored inside a container, this is a dynamically changing field, it is not an immutable */
	private Integer id, worldId;

	/** Rotating when in world */
	private float angle, angularVelocity = (Util.getRandom().nextFloat() - 0.5f) * 40f;

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
	public abstract String getSingular(boolean firstCap);

	/** Get the plural name for this item */
	public abstract String getPlural(boolean firstCap);

	/** Returns the string description of this {@link Item} */
	public abstract String getDescription();

	/** Returns true if two {@link Item}s have identical attributes */
	public abstract boolean sameAs(Item other);

	/** Gets the {@link TextureRegion} of this {@link Item} */
	protected abstract TextureRegion getTextureRegion();

	/** Whether to call standard {@link #update(float)} or {@link #updateRigid(float)} */
	protected boolean rotates() {
		return false;
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
		IronIngot.IRONINGOT = new TextureRegion(Domain.gameWorldTexture, 372, 246, 18, 6);
	}


	/** A window with a description of this {@link Item} */
	public Window getInfoWindow() {
		return new MessageWindow(
			getDescription(),
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			getSingular(true),
			true,
			100,
			100
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
			new ContextMenu(0, 0, new MenuItem(
				"You have multiple individuals selected",
				() -> {},
				Colors.UI_DARK_GRAY,
				Colors.UI_DARK_GRAY,
				Colors.UI_DARK_GRAY,
				null
			)),
			() -> {return Domain.getSelectedIndividuals().size() > 1;}
		);

		ContextMenu menu = new ContextMenu(0, 0,
			new MenuItem(
				"Show info",
				() -> {
					UserInterface.addLayeredComponent(
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
		return getSingular(true);
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
}