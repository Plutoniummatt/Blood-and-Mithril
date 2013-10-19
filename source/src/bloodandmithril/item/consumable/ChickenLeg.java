package bloodandmithril.item.consumable;


import bloodandmithril.Fortress;
import bloodandmithril.character.Individual;
import bloodandmithril.character.individuals.Elf;
import bloodandmithril.item.Item;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MessageWindow;
import bloodandmithril.ui.components.window.Window;

import com.badlogic.gdx.graphics.Color;

public class ChickenLeg extends Consumable {
	private static final long serialVersionUID = 327664484386522545L;

	/**
	 * Constructor
	 */
	public ChickenLeg() {
		super(0.25f, false, 5);
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
				Fortress.getMouseScreenX(),
				Fortress.getMouseScreenY(),
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
		return true;
	}


	@Override
	public Window getInfoWindow() {
		return new MessageWindow(
			"A chicken leg, this is dark meat and is the lower part of the leg.",
			Color.ORANGE,
			Fortress.getMouseScreenX(),
			Fortress.getMouseScreenY(),
			350,
			200,
			"Chicken leg",
			true,
			100,
			100
		);
	}


	@Override
	public boolean sameAs(Item other) {
		if (other instanceof ChickenLeg) {
			return true;
		}
		return false;
	}
}
