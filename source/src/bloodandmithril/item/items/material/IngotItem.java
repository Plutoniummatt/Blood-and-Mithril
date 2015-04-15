package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Smithing;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An ingot made from a type of {@link Metal}
 *
 * @author Matt
 *
 * @param <T> a type of {@link Metal}
 */
@Copyright("Matthew Peck 2014")
public class IngotItem extends bloodandmithril.item.items.material.MaterialItem implements Craftable {
	private static final long serialVersionUID = -5952793507591312790L;

	private Class<? extends Metal> metal;

	/**
	 * Constructor
	 */
	private IngotItem(Class<? extends Metal> metal) {
		super(1f, 5, false);
		this.metal = metal;
		setValue(Material.getMaterial(metal).getIngotValue());
	}


	/**
	 * Static instance getter
	 */
	public static IngotItem ingot(Class<? extends Metal> metal) {
		return new IngotItem(metal);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getProficiencies().getProficiency(Smithing.class).getLevel() >= Material.getMaterial(metal).getIngotCraftingLevel();
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return Material.getMaterial(metal).getRequiredMaterialsToCraftIngot();
	}


	@Override
	public float getCraftingDuration() {
		return Material.getMaterial(metal).getIngotCraftingDuration();
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(metal).getName() + " Ingot";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(metal).getName() + " Ingots";
	}


	@Override
	public String getDescription() {
		return "An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from " + Material.getMaterial(metal).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof IngotItem) {
			return metal.equals(((IngotItem) other).metal);
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return Material.getMaterial(metal).getIngotTextureRegion();
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return Material.getMaterial(metal).getIngotIconTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return ingot(metal);
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Smithing.class).increaseExperience(delta * 2f * Material.getMaterial(metal).getCombatMultiplier());
	}
}