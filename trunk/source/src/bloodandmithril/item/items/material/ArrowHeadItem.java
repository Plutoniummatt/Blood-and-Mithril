package bloodandmithril.item.items.material;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.character.proficiency.proficiencies.Smithing;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.Material;
import bloodandmithril.item.material.metal.Metal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

/**
 * An arrowhead, used to....make arrows
 *
 * @author Matt
 */
@Copyright("Matthew Peck 2015")
public class ArrowHeadItem extends bloodandmithril.item.items.material.MaterialItem implements Craftable {
	private static final long serialVersionUID = 5603774113269428754L;

	private Class<? extends Metal> metal;

	/**
	 * Constructor
	 */
	private ArrowHeadItem(Class<? extends Metal> metal) {
		super(0.1f, 1, false, IngotItem.ingot(metal).getValue() / 25);
		this.metal = metal;
	}


	/**
	 * Static instance getter
	 */
	public static ArrowHeadItem arrowHead(Class<? extends Metal> metal) {
		return new ArrowHeadItem(metal);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getProficiencies().getProficiency(Smithing.class).getLevel() >= Material.getMaterial(metal).getIngotCraftingLevel() + 2;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		HashMap<Item, Integer> materials = Maps.newHashMap();
		materials.put(Material.getMaterial(metal).getIngot(), 1);
		return materials;
	}


	@Override
	public float getCraftingDuration() {
		return 10;
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return Material.getMaterial(metal).getName() + " Arrowhead";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return Material.getMaterial(metal).getName() + " Arrowheads";
	}

	@Override
	public String getDescription() {
		return "An arrowhead is a tip, usually sharpened, added to an arrow to make it more deadly or to fulfill some special purpose.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		if (other instanceof ArrowHeadItem) {
			return metal.equals(((ArrowHeadItem) other).metal);
		}

		return false;
	}


	@Override
	public TextureRegion getTextureRegion() {
		// TODO
		return null;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		// TODO
		return null;
	}


	@Override
	protected Item internalCopy() {
		return arrowHead(metal);
	}


	@Override
	public void crafterEffects(Individual crafter, float delta) {
		crafter.getProficiencies().getProficiency(Smithing.class).increaseExperience(delta * 5f * Material.getMaterial(metal).getCombatMultiplier());
	}
}