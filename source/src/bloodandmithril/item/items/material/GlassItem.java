package bloodandmithril.item.items.material;

import java.util.Map;

import bloodandmithril.character.individuals.Individual;
import bloodandmithril.core.Copyright;
import bloodandmithril.item.Craftable;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.mineral.earth.SandItem;
import bloodandmithril.item.material.mineral.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.google.common.collect.Maps;

@Copyright("Matthew Peck 2014")
public class GlassItem extends bloodandmithril.item.items.material.MaterialItem implements Craftable {
	private static final long serialVersionUID = -1491126318224334985L;

	public static TextureRegion GLASS_ICON;
	public static TextureRegion GLASS_ITEM;

	/**
	 * Constructor
	 */
	public GlassItem() {
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
		return other instanceof GlassItem;
	}


	@Override
	public TextureRegion getTextureRegion() {
		return GLASS_ITEM;
	}


	@Override
	protected Item internalCopy() {
		return new GlassItem();
	}


	@Override
	public boolean canBeCraftedBy(Individual individual) {
		// TODO
		return true;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterials() {
		Map<Item, Integer> map = Maps.newHashMap();
		map.put(new SandItem(), 5);
		map.put(RockItem.rock(Coal.class), 1);
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


	@Override
	public void crafterEffects(Individual crafter, float delta) {
	}
}