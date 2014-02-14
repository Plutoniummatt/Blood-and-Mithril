package bloodandmithril.item;

import java.io.Serializable;

import bloodandmithril.character.Individual;
import bloodandmithril.ui.components.window.Window;


/**
 * An {@link Item}
 *
 * @author Matt
 */
public abstract class Item implements Serializable, Comparable<Item> {
	private static final long serialVersionUID = -7733840667288631158L;

	/** The mass of this item */
	public float mass;

	/** The value of this item */
	public long value;

	/** Whether this item can be equipped by an {@link Individual} */
	public final boolean equippable;

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
	public abstract Item combust(float temperature, float time);

	@Override
	public int compareTo(Item o) {
		if (value == o.value) {
			return getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
		} else {
			return value > o.value ? 1 : -1;
		}
	}
}
