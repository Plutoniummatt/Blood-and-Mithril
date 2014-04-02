package bloodandmithril.item;

import static bloodandmithril.world.topography.Topography.TILE_SIZE;
import static java.lang.Math.abs;

import java.io.Serializable;

import com.badlogic.gdx.math.Vector2;

import bloodandmithril.character.Individual;
import bloodandmithril.ui.components.window.Window;
import bloodandmithril.world.Domain;
import bloodandmithril.world.topography.tile.Tile;

/**
 * An {@link Item}
 *
 * @author Matt
 */
public abstract class Item implements Serializable, Comparable<Item> {
	private static final long serialVersionUID = -7733840667288631158L;

	/** The mass of this item */
	public final float mass;

	/** The value of this item */
	public final long value;

	/** Whether this item can be equipped by an {@link Individual} */
	public final boolean equippable;
	
	/** The position and velocity of this item, if it exists on the world */
	private Vector2 position, velocity;
	
	private Integer id;
	
	/**
	 * Constructor
	 */
	protected Item(float mass, boolean equippable, long value) {
		this.mass = mass;
		this.equippable = equippable;
		this.value = value;
	}

	/** Get the singular name for this item */
	public abstract String getSingular(boolean firstCap);

	/** Get the plural name for this item */
	public abstract String getPlural(boolean firstCap);

	/** A window with a description of this {@link Item} */
	public abstract Window getInfoWindow();

	/** Returns true if two {@link Item}s have identical attributes */
	public abstract boolean sameAs(Item other);

	/** What this {@link Item} will turn into when combusted */
	public abstract Item combust(int heatLevel);
	
	/** Implementation-specific render method */
	public abstract void render();
	
	/** Update method, delta measured in seconds */
	public void update(float delta) {
		position.add(velocity.cpy().mul(delta));
		
		float gravity = Domain.getWorld(id).getGravity();
		if (abs((velocity.y - gravity * delta) * delta) < TILE_SIZE/2) {
			velocity.y = velocity.y - delta * gravity;
		} else {
			velocity.y = velocity.y * 0.8f;
		}
		
		Tile tileUnder = Domain.getWorld(id).getTopography().getTile(position.x, position.y, true);
		if (tileUnder.isPlatformTile || !tileUnder.isPassable()) {
			position.y = Domain.getWorld(id).getTopography().getLowestEmptyTileOrPlatformTileWorldCoords(position, true).y;
			velocity.y = 0f;
		}
	}
	
	@Override
	public int compareTo(Item o) {
		if (value == o.value) {
			return getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
		} else {
			return value > o.value ? 1 : -1;
		}
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
}