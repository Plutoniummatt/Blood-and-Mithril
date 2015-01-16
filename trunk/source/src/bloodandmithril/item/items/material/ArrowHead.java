package bloodandmithril.item.items.material;

import java.util.HashMap;
import java.util.Map;

import bloodandmithril.character.individuals.Individual;
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
public class ArrowHead extends bloodandmithril.item.items.material.Material implements Craftable {
	private static final long serialVersionUID = 5603774113269428754L;

	private Class<? extends Metal> metal;

	/**
	 * Constructor
	 */
	private ArrowHead(Class<? extends Metal> metal) {
		super(0.1f, 1, false, Ingot.ingot(metal).getValue() / 25);
		this.metal = metal;
	}


	/**
	 * Static instance getter
	 */
	public static ArrowHead arrowHead(Class<? extends Metal> metal) {
		return new ArrowHead(metal);
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() >= Material.getMaterial(metal).getIngotCraftingLevel() + 2;
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
		if (other instanceof ArrowHead) {
			return metal.equals(((ArrowHead) other).metal);
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
}