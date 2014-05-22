package bloodandmithril.item.items.misc;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Class representing currency
 *
 * @author Matt
 */
public class Currency extends Misc {
	private static final long serialVersionUID = -7059495735666011863L;

	/**
	 * Constructor
	 */
	public Currency() {
		super(0f, false, ItemValues.CURRENCY);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Coin" : "coin";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Coins" : "coins";
	}


	@Override
	public String getDescription() {
		return "The most widely used form of currency, can be used for trade as a substitution for items.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Currency;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new Currency();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}