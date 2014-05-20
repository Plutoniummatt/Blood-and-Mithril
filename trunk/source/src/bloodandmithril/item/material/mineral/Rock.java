package bloodandmithril.item.material.mineral;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.Item;
import bloodandmithril.item.material.Material;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * A rock made from a {@link Mineral}
 *
 * @author Matt
 */
public class Rock extends Item implements Craftable {
	private static final long serialVersionUID = -7786694866208809183L;

	private Class<? extends Mineral> mineral;

	/**
	 * Private constructor
	 */
	private Rock(Class<? extends Mineral> mineral) {
		super(2f, false);
		this.mineral = mineral;
	}


	/**
	 * Static instance getter
	 */
	public static Rock rock(Class<? extends Mineral> mineral) {
		return new Rock(mineral);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		throw new IllegalStateException("Should not be able to craft a rock");
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		throw new IllegalStateException("Should not be able to craft a rock");
	}


	@Override
	public float getCraftingDuration() {
		throw new IllegalStateException("Should not be able to craft a rock");
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(mineral).getName();
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(mineral).getName();
	}


	@Override
	public String getDescription() {
		return Material.getMaterial(mineral).getMineralDescription();
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof Rock) {
			return mineral.equals(((Rock) other).mineral);
		}

		return false;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return Material.getMaterial(mineral).getRockTextureRegion();
	}


	@Override
	protected Item internalCopy() {
		return rock(mineral);
	}
}