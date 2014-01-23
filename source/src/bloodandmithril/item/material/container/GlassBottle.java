package bloodandmithril.item.material.container;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.material.liquid.Liquid;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * A {@link Bottle} made from glass
 *
 * @author Matt
 */
public class GlassBottle extends Bottle {

	/**
	 * Constructor
	 */
	public GlassBottle(Class<? extends Liquid> liquid, float amount) {
		super(0.1f, amount, 1f, liquid, 20);
	}


	@Override
	public String getSingular(boolean firstCap) {
		String content = "";
		if (containedLiquid != null && amount != 0f) {
			content = " of " + containedLiquid.getSimpleName() + " (" + String.format("%.2f", amount) + "/" + String.format("%.2f", maxAmount) + ")";
		}

		return (firstCap ? "G" : "g") + "lass bottle" + content;
	}


	@Override
	public String getPlural(boolean firstCap) {
		return null;
	}


	@Override
	public Window getInfoWindow() {
		try {
			return new MessageWindow(
				containedLiquid.newInstance().getDescription(),
				Color.WHITE,
				BloodAndMithrilClient.getMouseScreenX(),
				BloodAndMithrilClient.getMouseScreenY(),
				300,
				200,
				containedLiquid.getSimpleName(),
				true,
				300,
				200
			);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public Bottle clone() {
		return new GlassBottle(containedLiquid, amount);
	}
}