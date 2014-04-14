package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.character.Individual;
import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.equipment.Craftable;
import bloodandmithril.item.material.mineral.Hematite;

/**
 * Lump of iron
 *
 * @author Matt
 */
public class IronIngot extends Item implements Craftable {
	private static final long serialVersionUID = 5784780777572238051L;

	/**
	 * Constructor
	 */
	public IronIngot() {
		super(1f, false, ItemValues.IRONINGOT);
	}


	@Override
	public String getSingular(boolean firstCap) {
		return (firstCap ? "I" : "i") + "ron ingot";
	}


	@Override
	public String getPlural(boolean firstCap) {
		return (firstCap ? "I" : "i") + "ron ingots";
	}


	@Override
	public String getDescription() {
		return "An ingot is a material, usually metal, that is cast into a shape suitable for further processing, this one is made from Iron.";
	}


	@Override
	public boolean sameAs(Item other) {
		return other instanceof IronIngot;
	}


	@Override
	public void render() {
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getSmithing() > 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = newHashMap();
		map.put(new Hematite(), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 2f;
	}
}