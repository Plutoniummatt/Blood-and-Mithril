package bloodandmithril.item.material.animal;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MessageWindow;

import com.badlogic.gdx.graphics.Color;

public class ChickenLeg extends Item implements Consumable {
	private static final long serialVersionUID = 327664484386522545L;

	/**
	 * Constructor
	 */
	public ChickenLeg() {
		super(0.25f, false, ItemValues.CHICKENLEG);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return firstCap ? "Chicken leg" : "chicken leg";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return firstCap ? "Chicken legs" : "chicken legs";
	}


	@Override
	public boolean consume(Individual consumer) {
		if (consumer instanceof Elf) {

			MessageWindow messageWindow = new MessageWindow(
				"Elves are vegans, they do not eat meat.",
				Color.RED,
				BloodAndMithrilClient.WIDTH/2 - 175,
				BloodAndMithrilClient.HEIGHT/2 + 100,
				470,
				120,
				"Info",
				true,
				100,
				100
			);

			UserInterface.addLayeredComponent(messageWindow);
			return false;
		}
		consumer.increaseHunger(0.15f);
		return true;
	}


	@Override
	public String getDescription() {
		return "A chicken leg, this is dark meat and is the lower part of the leg.";
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof ChickenLeg) {
			return true;
		}
		return false;
	}


	@Override
	public Item combust(int heatLevel, Map<Item, Integer> with) {
		return this;
	}


	@Override
	public void render() {

	}
}