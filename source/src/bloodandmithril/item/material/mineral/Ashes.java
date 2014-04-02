package bloodandmithril.item.material.mineral;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

public class Ashes extends Item {
	private static final long serialVersionUID = 988154990456038686L;
	public static final String description = "The residue of combustion, mostly consisting of metal oxides.";
	
	/**
	 * Constructor
	 */
	public Ashes() {
		super(0.2f, false, 0);
	}

	
	@Override
	public String getSingular(boolean firstCap) {
		return "Ashes";
	}

	
	@Override
	public String getPlural(boolean firstCap) {
		return "Ashes";
	}

	
	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			description,
			Color.ORANGE,
			BloodAndMithrilClient.WIDTH/2 - 175,
			BloodAndMithrilClient.HEIGHT/2 + 100,
			350,
			200,
			"Ashes",
			true,
			100,
			100
		);
	}

	
	@Override
	public boolean sameAs(Item other) {
		return other instanceof Ashes;
	}

	
	@Override
	public Item combust(int heatLevel) {
		return this;
	}


	@Override
	public void render() {
		
	}
}