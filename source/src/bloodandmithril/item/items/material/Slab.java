package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.mineral.Mineral;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A slab made from a {@link Mineral}
 *
 * @author Matt
 */
public class Slab extends bloodandmithril.item.items.material.Material implements Craftable {
	private static final long serialVersionUID = -7786694866208809183L;

	private Class<? extends Mineral> mineral;

	/**
	 * Private constructor
	 */
	private Slab(Class<? extends Mineral> mineral) {
		super(2f, false);
		this.mineral = mineral;
		setValue(Material.getMaterial(mineral).getSlabValue());
	}


	/**
	 * Static instance getter
	 */
	public static Slab slab(Class<? extends Mineral> mineral) {
		return new Slab(mineral);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getMasonry() >= Material.getMaterial(mineral).getSlabCraftingLevel();
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		return Material.getMaterial(mineral).getRequiredMaterialsToCraftSlab();
	}


	@Override
	public float getCraftingDuration() {
		return Material.getMaterial(mineral).getSlabCraftingDuration();
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(mineral).getName() + " Slab";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(mineral).getName() + " Slabs";
	}


	@Override
	public String getDescription() {
		return "A large, thick, flat piece of rock, made from " + Material.getMaterial(mineral).getName() + ".";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Slab) {
			return mineral.equals(((Slab) other).mineral);
		}

		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Material.getMaterial(mineral).getSlabTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return slab(mineral);
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO Auto-generated method stub
		return null;
	}
}