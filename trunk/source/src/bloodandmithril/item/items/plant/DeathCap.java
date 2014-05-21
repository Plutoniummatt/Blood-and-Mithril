package bloodandmithril.item.items.plant;

import bloodandmithril.character.Individual;
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.item.Consumable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * Poisonous mushroom
 *
 * @author Matt
 */
public class DeathCap extends Item implements Consumable {
	private static final long serialVersionUID = -7125731448429674227L;

	private final boolean cooked;

	/**
	 * Constructor
	 */
	public DeathCap(boolean cooked) {
		super(0.01f, false, ItemValues.DEATHCAP);
		this.cooked = cooked;
	}


	@Override
	public boolean consume(Individual consumer) {
		if (cooked) {
			consumer.increaseHunger(0.1f);
		} else {
			consumer.addCondition(new Poison(0.1f, 0.001f));
		}
		return true;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "Death cap" : "death cap") + (cooked ? " (Cooked)" : " (Raw)");
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "Death caps" : "death caps") + (cooked ? " (Cooked)" : " (Raw)");
	}


	@Override
	public String getDescription() {
		return "The death cap is a toxic fungus, the toxins breakdown once cooked, and is widely used in cuisine across the land.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof DeathCap) {
			return cooked == ((DeathCap)other).cooked;
		}
		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new DeathCap(cooked);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}