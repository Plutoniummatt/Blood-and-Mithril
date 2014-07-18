package bloodandmithril.item.affix;

import java.io.Serializable;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * An affix is an attribute that can be attached to {@link Item}s
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class Affix implements Serializable {
	private static final long serialVersionUID = 4446739414723141218L;

	/**
	 * @param name the name without the affix application
	 * @return The modified name of the item.
	 */
	public abstract String modifyName(String name);

	/**
	 * @return whether this {@link Affix} is the same as another
	 */
	public abstract boolean isSameAs(Affix other);
}