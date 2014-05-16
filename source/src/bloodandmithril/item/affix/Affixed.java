package bloodandmithril.item.affix;

import java.util.List;

import bloodandmithril.item.Item;

/**
 * Interface to indicate that an {@link Item} is affixed
 *
 * @author Matt
 */
public interface Affixed {

	/**
	 * @return all affixes
	 */
	public List<Affix> getAffixes();

	/**
	 * @return the modified name of the item
	 */
	public String modifyName(String original);
}