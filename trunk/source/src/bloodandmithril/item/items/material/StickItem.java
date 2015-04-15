package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Carpentry;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.wood.Wood;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

@Copyright("Matthew Peck 2015")
public class StickItem extends bloodandmithril.item.items.material.MaterialItem implements Craftable {
	private static final long serialVersionUID = 2206871386989760859L;

	private Class<? extends Wood> wood;

	public static TextureRegion STICK;

	/**
	 * Constructor
	 */
	private StickItem(Class<? extends Wood> wood) {
		super(0.2f, 1, false, ItemValues.WOODSTICK);
		this.wood = wood;
	}


	/**
	 * Static instance getter
	 */
	public static StickItem stick(Class<? extends Wood> wood) {
		return new StickItem(wood);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Stick";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(wood).getName() + " Sticks";
	}


	@Override
	public String getDescription() {
		return "A stick, made from " + Material.getMaterial(wood).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof StickItem) {
			return wood.equals(((StickItem) other).wood);
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return STICK;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO
		return null;
	}


	@Override
	protected Item internalCopy() {
		return stick(wood);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return Material.getMaterial(wood).getRequiredMaterialsToCraftStick();
	}


	@Override
	public float getCraftingDuration() {
		return Material.getMaterial(wood).getStickCraftingDuration();
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Carpentry.class).increaseExperience(delta * 3f);
	}
}