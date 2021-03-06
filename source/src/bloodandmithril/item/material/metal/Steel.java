package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.core.Copyright;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.IngotItem;
import bloodandmithril.item.items.material.RockItem;
import bloodandmithril.item.material.mineral.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

@Copyright("Matthew Peck 2014")
public class Steel extends Metal {
	private static final long serialVersionUID = 5907951500410886363L;

	public static TextureRegion STEELINGOT;
	public static TextureRegion STEELINGOTICON;

	/**
	 * Package protected constructor
	 */
	Steel() {}


	@Override
	public long getIngotValue() {
		return ItemValues.STEELINGOT;
	}


	@Override
	public int getIngotCraftingLevel() {
		return 15;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftIngot() {
		Map<Item, Integer> map = newHashMap();
		map.put(IngotItem.ingot(Iron.class), 1);
		map.put(RockItem.rock(Coal.class), 1);
		return map;
	}


	@Override
	public float getIngotCraftingDuration() {
		return 5f;
	}


	@Override
	public TextureRegion getIngotTextureRegion() {
		return STEELINGOT;
	}


	@Override
	public TextureRegion getIngotIconTextureRegion() {
		return STEELINGOTICON;
	}


	@Override
	public String getName() {
		return "Steel";
	}


	@Override
	public float getCombatMultiplier() {
		return 1.25f;
	}
}