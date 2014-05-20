package bloodandmithril.item.material.metal;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import bloodandmithril.item.Item;
import bloodandmithril.item.ItemValues;
import bloodandmithril.item.material.fuel.Coal;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Steel extends Metal {

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
		map.put(new Coal(), 1);
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