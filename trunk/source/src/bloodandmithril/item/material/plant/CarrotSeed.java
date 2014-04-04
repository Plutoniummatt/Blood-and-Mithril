package bloodandmithril.item.material.plant;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

public class CarrotSeed extends Seed {
	private static final long serialVersionUID = -3918937697003306522L;

	/**
	 * Constructor
	 */
	protected CarrotSeed() {
		super(0.001f, false, ItemValues.CARROTSEED);
	}

	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Carrot seed" : "carrot seed";
	}

	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Carrot seeds" : "carrot seeds";
	}

	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"Seed of a carrot",
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Carrot seed",
			true,
			100,
			100
		);
	}

	@Override
	public boolean sameAs(Item other) {
		return other instanceof CarrotSeed;
	}

	@Override
	public Item combust(int heatLevel) {
		return this;
	}

	@Override
	public void render() {
		
	}
}