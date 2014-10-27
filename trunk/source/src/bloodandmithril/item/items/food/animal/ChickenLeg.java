package bloodandmithril.item.items.food.animal;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MessageWindow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

@Copyright("Matthew Peck 2014")
public class ChickenLeg extends Food {
	private static final long serialVersionUID = 327664484386522545L;

	/**
	 * Constructor
	 */
	public ChickenLeg() {
		super(0.25f, 3, false, ItemValues.CHICKENLEG);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Chicken leg" : "chicken leg";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
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
	protected boolean internalSameAs(Item other) {
		if (other instanceof ChickenLeg) {
			return true;
		}
		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new ChickenLeg();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}