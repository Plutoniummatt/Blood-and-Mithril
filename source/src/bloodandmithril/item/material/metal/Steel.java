package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.ItemValues;
import bloodandmithril.item.items.Item;
import bloodandmithril.item.items.material.Ingot;
import bloodandmithril.item.items.material.Rock;
import bloodandmithril.item.material.mineral.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Steel extends Metal {
	private static final long serialVersionUID = 5907951500410886363L;

	public static TextureRegion STEELINGOT;

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
		map.put(Ingot.ingot(Iron.class), 1);
		map.put(Rock.rock(Coal.class), 1);
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
	public String getName() {
		return "Steel";
	}
}