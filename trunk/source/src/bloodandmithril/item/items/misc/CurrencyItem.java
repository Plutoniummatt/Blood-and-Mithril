package bloodandmithril.item.items.misc;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Class representing currency
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class CurrencyItem extends MiscItem {
	private static final long serialVersionUID = -7059495735666011863L;
	
	public static TextureRegion CURRENCY_POUCH;

	/**
	 * Constructor
	 */
	public CurrencyItem() {
		super(0f, 0, false, ItemValues.CURRENCY);
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
		return other instanceof CurrencyItem;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return CURRENCY_POUCH;
	}


	@Override
	protected Item internalCopy() {
		return new CurrencyItem();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}