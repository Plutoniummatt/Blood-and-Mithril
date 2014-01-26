package bloodandmithril.item.misc;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * Class representing currency
 *
 * @author Matt
 */
public class Currency extends Item {
	private static final long serialVersionUID = -7059495735666011863L;

	/**
	 * Constructor
	 */
	public Currency() {
		super(0f, false, 1);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Coin" : "coin";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Coins" : "coins";
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"The most widely used form of currency, can be used for trade as a substitution for items.",
			Color.ORANGE,
			BloodAndMithrilClient.getMouseScreenX(),
			BloodAndMithrilClient.getMouseScreenY(),
			350,
			200,
			"Coin",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof Currency;
	}
}