package bloodandmithril.item.material.metal;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * An ingot made from a type of {@link Metal}
 *
 * @author Matt
 *
 * @param <T> a type of {@link Metal}
 */
public class Ingot extends Item implements Craftable {
	private static final long serialVersionUID = -5952793507591312790L;

	private Class<? extends Metal> metal;

	/**
	 * Constructor
	 */
	private Ingot(Class<? extends Metal> metal) {
		super(1f, false);
		this.metal = metal;
	}


	/**
	 * Static instance getter
	 */
	public static Ingot ingot(Class<? extends Metal> metal) {
		return new Ingot(metal);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= Material.getMaterial(metal).getIngotCraftingLevel();
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
		if (other instanceof Ingot) {
			return metal.equals(((Ingot) other).metal);
		}

		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Material.getMaterial(metal).getIngotTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return ingot(metal);
	}
}