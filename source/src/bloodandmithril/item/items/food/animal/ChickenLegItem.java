package bloodandmithril.item.items.food.animal;

import java.util.Map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.individuals.characters.Elf;
import bloodandmithril.character.proficiency.proficiencies.Cooking;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;
import bloodandmithril.ui.UserInterface;
import bloodandmithril.ui.components.window.MessageWindow;

@Copyright("Matthew Peck 2014")
public class ChickenLegItem extends Food implements Craftable {
	private static final long serialVersionUID = 327664484386522545L;
	private boolean cooked;

	public static TextureRegion COOKED_CHICKEN_LEG;
	public static TextureRegion RAW_CHICKEN_LEG;

	public static TextureRegion COOKED_CHICKEN_LEG_ICON;
	public static TextureRegion RAW_CHICKEN_LEG_ICON;

	/**
	 * Constructor
	 */
	public ChickenLegItem(final boolean cooked) {
		super(0.25f, 3, false, cooked ? ItemValues.CHICKENLEG : ItemValues.COOKEDCHICKENLEG);
		this.cooked = cooked;
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		if (cooked) {
			return firstCap ? "Cooked chicken leg" : "cooked chicken leg";
		} else {
			return firstCap ? "Raw chicken leg" : "raw chicken leg";
		}
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		if (cooked) {
			return firstCap ? "Cooked chicken legs" : "cooked chicken legs";
		} else {
			return firstCap ? "Raw chicken legs" : "raw chicken legs";
		}
	}


	@Override
	public boolean consume(final Individual consumer) {
		if (consumer instanceof Elf) {

			final MessageWindow messageWindow = new MessageWindow(
				"Elves are vegans, they do not eat meat.",
				Color.RED,
				470,
				120,
				"Info",
				true,
				100,
				100
			);

			Wiring.injector().getInstance(UserInterface.class).addLayeredComponent(messageWindow);
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
	protected boolean internalSameAs(final Item other) {
		if (other instanceof ChickenLegItem) {
			return ((ChickenLegItem) other).cooked == this.cooked;
		}
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return cooked ? COOKED_CHICKEN_LEG : RAW_CHICKEN_LEG;
	}


	@Override
	protected Item internalCopy() {
		return new ChickenLegItem(cooked);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return cooked ? COOKED_CHICKEN_LEG_ICON	 : RAW_CHICKEN_LEG_ICON;
	}


	@Override
	public boolean canBeCraftedBy(final Individual individual) {
		return individual.getProficiencies().getProficiency(Cooking.class).getLevel() >= 5;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		final Map<Item, Integer> map = Maps.newHashMap();
		map.put(new ChickenLegItem(false), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 10;
	}


	@Override
	public void crafterEffects(final Individual crafter, final float delta) {
		crafter.getProficiencies().getProficiency(Cooking.class).increaseExperience(delta * 3f);
	}
}