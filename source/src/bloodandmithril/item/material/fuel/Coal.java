package bloodandmithril.item.material.fuel;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Fuel;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

public class Coal extends Item implements Fuel {
	private static final long serialVersionUID = 6399640412435082388L;
	
	/**
	 * Constructor
	 */
	public Coal() {
		super(0.5f, false, 10);
	}


	@Override
	public float getCombustionDuration() {
		return 120;
	}

	
	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Coal" : "coal";
	}

	
	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Coal" : "coal";
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"Coal is a combustible black or brownish-black sedimentary rock usually occurring in rock strata in layers or veins called coal beds or coal seams",
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Carrot",
			true,
			100,
			100
		);
	}

	
	@Override
	public boolean sameAs(Item other) {
		return other instanceof Coal;
	}

	
	@Override
	public Item combust(float temperature, float time) {
		return this;
	}
}