package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.item.material.fuel.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Lump of Steel
 *
 * @author Matt
 */
public class SteelIngot extends Item implements Craftable {
	private static final long serialVersionUID = -5395254759014196508L;

	/**
	 * Constructor
	 */
	public SteelIngot() {
		super(1f, false, ItemValues.STEELINGOT);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "S" : "s") + "teel ingot";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "S" : "s") + "teel ingots";
	}


	@Override
	public String getDescription() {
		return "An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from Steel.";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof SteelIngot;
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() > 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = newHashMap();
		map.put(new IronIngot(), 1);
		map.put(new Coal(), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 5f;
	}


	@Override
	protected TextureRegion getTextureRegion() {
		return null;
	}


	@Override
	protected Item internalCopy() {
		return new SteelIngot();
	}
}