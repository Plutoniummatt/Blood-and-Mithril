package bloodandmithril.item.items.food.plant;

import static bloodandmithril.character.ai.perception.Visible.getVisible;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import bloodandmithril.audio.SoundService;
import bloodandmithril.character.conditions.Poison;
import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.core.Wiring;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.food.Food;
import bloodandmithril.ui.FloatingTextService;


/**
 * Poisonous mushroom
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2014")
public class DeathCapItem extends Food {
	private static final long serialVersionUID = -7125731448429674227L;

	public static TextureRegion DEATH_CAP;
	private final boolean cooked;

	/**
	 * Constructor
	 */
	public DeathCapItem(final boolean cooked) {
		super(0.01f, 1, false, ItemValues.DEATHCAP);
		this.cooked = cooked;
	}


	@Override
	public boolean consume(final Individual consumer) {
		final FloatingTextService floatingTextService = Wiring.injector().getInstance(FloatingTextService.class);

		SoundService.play(SoundService.crunch, consumer.getState().position, true, getVisible(consumer));
		if (cooked) {
			consumer.increaseHunger(0.1f);
			floatingTextService.addFloatingTextToIndividual(consumer, "+10 Hunger", Color.ORANGE);
		} else {
			consumer.addCondition(new Poison(0.1f, 0.001f));
			floatingTextService.addFloatingTextToIndividual(consumer, "Poisoned!", Color.GREEN);
		}
		return true;
	}


	@Override
	protected String internalGetSingular(final boolean firstCap) {
		return (firstCap ? "Death cap" : "death cap") + (cooked ? " (Cooked)" : " (Raw)");
	}


	@Override
	protected String internalGetPlural(final boolean firstCap) {
		return (firstCap ? "Death caps" : "death caps") + (cooked ? " (Cooked)" : " (Raw)");
	}


	@Override
	public String getDescription() {
		return "The death cap is a toxic fungus, the toxins breakdown once cooked, and is widely used in cuisine across the land.";
	}


	@Override
	protected boolean internalSameAs(final Item other) {
		if (other instanceof DeathCapItem) {
			return cooked == ((DeathCapItem)other).cooked;
		}
		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return DEATH_CAP;
	}


	@Override
	protected Item internalCopy() {
		return new DeathCapItem(cooked);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}