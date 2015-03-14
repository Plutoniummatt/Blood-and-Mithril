package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.mineral.earth.Sand;
import bloodandmithril.item.material.mineral.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class Glass extends bloodandmithril.item.items.material.Material implements Craftable {
	private static final long serialVersionUID = -1491126318224334985L;

	public static TextureRegion GLASS_ICON;
	public static TextureRegion GLASS_ITEM;

	/**
	 * Constructor
	 */
	public Glass() {
		super(0.5f, 4, false, ItemValues.GLASS);
	}


	@Override
	protected String internalGetSingular(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass";
	}


	@Override
	protected String internalGetPlural(boolean firstCap) {
		return (firstCap ? "G" : "g") + "lass";
	}


	@Override
	public String getDescription() {
		return "Silicate glass";
	}


	@Override
	protected boolean internalSameAs(Item other) {
		return other instanceof Glass;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return GLASS_ITEM;
	}


	@Override
	protected Item internalCopy() {
		return new Glass();
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		return individual.getSkills().getGlassworking().getLevel() >= 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();
		map.put(new Sand(), 5);
		map.put(Rock.rock(Coal.class), 1);
		return map;
	}


	@Override
	public float getCraftingDuration() {
		return 2f;
	}


	@Override
	public TextureRegion getIconTextureRegion() {
		return null;
	}
}