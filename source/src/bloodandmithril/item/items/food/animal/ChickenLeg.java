package bloodandmithril.item.items.food.animal;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.core.BloodAndMithrilClient;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MessageWindow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class ChickenLeg extends Food implements Craftable {
	private static final long serialVersionUID = 327664484386522545L;
	private boolean cooked;

	/**
	 * Constructor
	 */
	public ChickenLeg(boolean cooked) {
		super(0.25f, 3, false, cooked ? ItemValues.CHICKENLEG : ItemValues.COOKEDCHICKENLEG);
		this.cooked = cooked;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		if (cooked) {
			return firstCap ? "Cooked chicken leg" : "cooked chicken leg";
		} else {
			return firstCap ? "Raw chicken leg" : "raw chicken leg";
		}
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		if (cooked) {
			return firstCap ? "Cooked chicken legs" : "cooked chicken legs";
		} else {
			return firstCap ? "Raw chicken legs" : "raw chicken legs";
		}
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
			return ((ChickenLeg) other).cooked == this.cooked;
		}
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new ChickenLeg(cooked);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getCookking().getLevel() >= 5;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();
		map.put(new ChickenLeg(false), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 10;
	}
}