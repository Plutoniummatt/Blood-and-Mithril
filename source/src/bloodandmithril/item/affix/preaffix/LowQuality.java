package bloodandmithril.item.affix.preaffix;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.affix.Affix;
import bloodandmithril.item.affix.PreAffix;

@Copyright("Matthew Peck 2014")
public class LowQuality extends PreAffix {
	private static final long serialVersionUID = 5750270806066218014L;
	private final float valueModifier;

	/**
	 * Constructor
	 */
	public LowQuality(float valueModifier) {
		this.valueModifier = valueModifier;
	}


	@Override
	protected String getPreAffixDescription() {
		return "Low Quality";
	}


	@Override
	public boolean isSameAs(Affix other) {
		return other instanceof LowQuality && ((LowQuality)other).valueModifier == valueModifier;
	}
}