package bloodandmithril.item.material.plant;

import com.badlogic.gdx.graphics.Color;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;


/**
 * Felberries
 *
 * @author Matt
 */
public class Felberries extends Item implements Consumable {
	private static final long serialVersionUID = 3833984831172862989L;
	private static String description = "Felberries grow naturally in most climates, a good source of fiber as well as having wound healing properties";

	/**
	 * Constructor
	 */
	public Felberries() {
		super(0.1f, false, 3);
	}


	@Override
	public boolean consume(Individual consumer) {
		consumer.increaseHunger(0.05f);
		consumer.heal(0.5f);
		return true;
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Felberries" : "felberries";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Felberries" : "felberries";
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
	public boolean sameAs(Item other) {
		return other instanceof Felberries;
	}


	@Override
	public Item combust(float temperature, float time) {
		return this;
	}
}