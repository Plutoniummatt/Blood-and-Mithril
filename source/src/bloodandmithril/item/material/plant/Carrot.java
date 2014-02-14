package bloodandmithril.item.material.plant;

import bloodandmithril.BloodAndMithrilClient;
import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

/**
 * A Carrot
 *
 * @author Matt
 */
public class Carrot extends Item implements Consumable {
	private static final long serialVersionUID = 3714624810622084079L;
	public static final String description = "The carrot is a root vegetable, usually orange in color. It has a crisp texture when fresh.";

	/**
	 * Constructor
	 */
	public Carrot() {
		super(0.1f, false, 5);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Carrot" : "carrot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Carrots" : "carrots";
	}


	@Override
	public boolean consume(Individual consumer) {
		consumer.increaseHunger(0.05f);
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
	public boolean sameAs(Item other) {
		if (other instanceof Carrot) {
			return true;
		}
		return false;
	}


	public static class CarrotSeed extends Seed {
		private static final long serialVersionUID = -3918937697003306522L;

		/**
		 * Constructor
		 */
		protected CarrotSeed(float mass, boolean equippable, long value) {
			super(mass, equippable, value);
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
		public Item combust(float temperature, float time) {
			return this;
		}
	}


	@Override
	public Item combust(float temperature, float time) {
		return this;
	}
}