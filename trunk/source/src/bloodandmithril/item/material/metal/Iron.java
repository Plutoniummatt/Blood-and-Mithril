package bloodandmithril.item.material.metal;

import static bloodandmithril.item.items.material.Rock.rock;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.material.mineral.Hematite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Iron extends Metal {
	private static final long serialVersionUID = -338323130439901358L;

	public static TextureRegion IRONINGOT;

	/**
	 * Package protected constructor
	 */
	Iron() {}


	@Override
	public long getIngotValue() {
		return ItemValues.IRONINGOT;
	}


	@Override
	public int getIngotCraftingLevel() {
		return 0;
	}


	@Override
	public Map<Item, Integer> getRequiredMaterialsToCraftIngot() {
		Map<Item, Integer> map = newHashMap();
		map.put(rock(Hematite.class), 1);
		return map;
	}


	@Override
	public float getIngotCraftingDuration() {
		return 2f;
	}


	@Override
	public TextureRegion getIngotTextureRegion() {
		return IRONINGOT;
	}


	@Override
	public String getName() {
		return "Iron";
	}
}