package bloodandmithril.item.equipment;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * {@link Ring} made of gold
 *
 * @author Matt
 */
public class GoldRing extends Ring {
	private static final long serialVersionUID = 3637271449455734873L;

	/**
	 * Constructor
	 */
	public GoldRing(long value) {
		super(value);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return false; //TODO crafting gold ring
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return null; //TODO crafting gold ring
	}


	@Override
	public float getCraftingDuration() {
		return 0; //TODO crafting gold ring
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "G" : "g") + "old ring";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "G" : "g") + "old rings";
	}


	@Override
	public String getDescription() {
		return "A ring made of gold";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof GoldRing && other.getValue() == getValue();
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new GoldRing(getValue());
	}
}