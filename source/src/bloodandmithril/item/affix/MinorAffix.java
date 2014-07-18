package bloodandmithril.item.affix;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.items.Item;

/**
 * Minor affixes are anything but {@link PostAffix} or {@link PreAffix}, an {@link Item} may have any number of these
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public abstract class MinorAffix extends Affix {
	private static final long serialVersionUID = -7299714683100264290L;

	@Override
	public String modifyName(String name) {
		return name;
	}
}