package bloodandmithril.item.affix;

import java.util.List;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Interface to indicate that an {@link Item} is affixed
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public interface Affixed {

	/**
	 * @return all affixes
	 */
	public List<MinorAffix> getMinorAffixes();

	/**
	 * @return the {@link PostAffix}
	 */
	public Affix getPostAffix();

	/**
	 * @return the {@link PreAffix}
	 */
	public Affix getPreAffix();

	/**
	 * Sets the {@link PostAffix}
	 */
	public void setPostAffix(PostAffix postAffix);

	/**
	 * Sets the {@link PreAffix}
	 */
	public void setPreAffix(PreAffix preAffix);

	/**
	 * @return the modified name of the item
	 */
	public String modifyName(String original);
}