package bloodandmithril.item.material.plant;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CookedCarrot extends Item implements Consumable {
	private static final long serialVersionUID = -4630040294684060393L;
	public static final String description = "A nicely cooked carrot, less crunchy than raw.  Increases stamina as well as hunger";

	/**
	 * Constructor
	 */
	public CookedCarrot() {
		super(0.1f, false, ItemValues.COOKEDCARROT);
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
	public String getDescription() {
		return description;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}
}