package bloodandmithril.item.material.plant;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

public class CookedCarrot extends Item implements Consumable {
	private static final long serialVersionUID = -4630040294684060393L;
	public static final String description = "A nicely cooked carrot, less crunchy than raw.  Increases stamina as well as hunger";
	
	/**
	 * Constructor
	 */
	public CookedCarrot() {
		super(0.1f, false, 10);
	}
	
	
	@Override
	public boolean sameAs(Item other) {
		if (other instanceof CookedCarrot) {
			return true;
		}
		return false;
	}
	
	
	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Cooked carrot" : "cooked carrot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Cooked carrots" : "cooked carrots";
	}


	@Override
	public boolean consume(Individual consumer) {
		consumer.increaseHunger(0.05f);
		consumer.increaseStamina(0.10f);
		return true;
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
			"Carrot",
			true,
			100,
			100
		);
	}


	@Override
	public Item combust(int heatLevel) {
		return null;
	}


	@Override
	public void render() {
		
	}
}