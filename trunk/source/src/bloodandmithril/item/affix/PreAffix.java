package bloodandmithril.item.affix;

import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public abstract class PreAffix extends Affix {
	private static final long serialVersionUID = -870702724623019310L;

	@Override
	public String modifyName(String name) {
		return getPreAffixDescription() + " " + name;
	}


	/**
	 * @return the pre affix description, ie "Shiny Gold ring of The Tiger", this method will return "Shiny".
	 */
	protected abstract String getPreAffixDescription();
}