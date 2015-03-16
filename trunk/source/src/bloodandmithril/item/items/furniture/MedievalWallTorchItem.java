package bloodandmithril.item.items.furniture;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.prop.Prop;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MedievalWallTorchItem extends FurnitureItem implements Craftable {
	private static final long serialVersionUID = 1841442106486694024L;

	/**
	 * Constructor
	 */
	public MedievalWallTorchItem() {
		super(1.5f, 10, ItemValues.MEDIEVALWALLTORCH);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return null;
	}


	@Override
	public float getCraftingDuration() {
		return 10f;
	}


	@Override
	public Prop getProp() {
		return new bloodandmithril.prop.furniture.MedievalWallTorchProp(0, 0);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return "Medieval wall torch";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return "Medieval wall torch";
	}


	@Override
	public String getDescription() {
		return "A simple medieval wall torch, can be placed on walls";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof MedievalWallTorchItem;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new MedievalWallTorchItem();
	}
}