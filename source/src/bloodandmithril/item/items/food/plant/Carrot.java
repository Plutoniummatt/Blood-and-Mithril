package bloodandmithril.item.items.food.plant;

import bloodandmithril.character.Individual;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A Carrot
 *
 * @author Matt
 */
public class Carrot extends Food {
	private static final long serialVersionUID = 3714624810622084079L;
	public static final String description = "The carrot is a root vegetable, usually orange in color. It has a crisp texture when fresh.";

	public static TextureRegion CARROT;

	/**
	 * Constructor
	 */
	public Carrot() {
		super(0.1f, false, ItemValues.CARROT);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return firstCap ? "Carrot" : "carrot";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return firstCap ? "Carrots" : "carrots";
	}


	@Override
	public boolean consume(Individual consumer) {
		consumer.increaseHunger(0.05f);
		return true;
	}


	@Override
	public String getDescription() {
		return description;
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Carrot) {
			return true;
		}
		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return CARROT;
	}


	@Override
	protected Item internalCopy() {
		return new Carrot();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}