package bloodandmithril.item.affix;

import bloodandmithril.core.Copyright;

@Copyright("Matthew Peck 2014")
public abstract class PostAffix extends Affix {
	private static final long serialVersionUID = -870702724623019310L;

	@Override
	public String modifyName(String name) {
		return name + " of " + getPostAffixDescription();
	}


	/**
	 * @return the post affix description, ie "Gold ring of the tiger", this method will return "the tiger".
	 */
	protected abstract String getPostAffixDescription();
}