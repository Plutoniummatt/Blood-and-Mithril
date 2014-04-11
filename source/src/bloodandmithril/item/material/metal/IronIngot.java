package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.fuel.Coal;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;
import com.google.common.collect.Iterables;

/**
 * Lump of iron
 *
 * @author Matt
 */
public class IronIngot extends Item {
	private static final long serialVersionUID = 5784780777572238051L;

	/**
	 * Constructor
	 */
	public IronIngot() {
		super(1f, false, 15);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "I" : "i") + "ron ingot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "I" : "i") + "ron ingots";
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from Iron.",
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
		return other instanceof IronIngot;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		boolean hasCoal = Iterables.tryFind(with.entrySet(), entry -> {
			return entry.getKey() instanceof Coal;
		}).isPresent();

		if (heatLevel >= 1400 && hasCoal) {
			return new SteelIngot();
		}
		return this;
	}


	@Override
	public void render() {
	}
}