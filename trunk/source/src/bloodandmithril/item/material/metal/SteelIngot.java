package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * Lump of Steel
 *
 * @author Matt
 */
public class SteelIngot extends Item {
	private static final long serialVersionUID = -5395254759014196508L;

	/**
	 * Constructor
	 */
	public SteelIngot() {
		super(1f, false, 20);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "S" : "s") + "teel ingot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "S" : "s") + "teel ingots";
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from Steel.",
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Iron ingot",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof SteelIngot;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {
	}
}