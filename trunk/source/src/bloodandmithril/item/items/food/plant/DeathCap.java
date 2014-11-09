package bloodandmithril.item.items.food.plant;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


/**
 * Poisonous mushroom
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DeathCap extends Food {
	private static final long serialVersionUID = -7125731448429674227L;

	private final boolean cooked;

	/**
	 * Constructor
	 */
	public DeathCap(boolean cooked) {
		super(0.01f, 1, false, ItemValues.DEATHCAP);
		this.cooked = cooked;
	}


	@Override
	public boolean consume(Individual consumer) {
		SoundService.play(6, consumer.getState().position, true);
		if (cooked) {
			consumer.increaseHunger(0.1f);
			consumer.addFloatingText("+10 Hunger", Color.ORANGE);
		} else {
			consumer.addCondition(new Poison(0.1f, 0.001f));
			consumer.addFloatingText("Poisoned!", Color.GREEN);
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
	public TextureRegion getTextureRegion() {
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